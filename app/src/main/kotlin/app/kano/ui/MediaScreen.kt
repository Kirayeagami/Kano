package app.kano.ui

import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import app.kano.ui.KanoButton as Button
import androidx.compose.material3.LinearProgressIndicator
import app.kano.ui.KanoOutlinedButton as OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import app.kano.data.MediaRecord

@Composable fun MediaScreen(model: KanoViewModel) {
    val rows by model.rows.collectAsStateWithLifecycle()
    val count by model.count.collectAsStateWithLifecycle()
    val query by model.query.collectAsStateWithLifecycle()
    val page by model.page.collectAsStateWithLifecycle()
    val busy by model.busy.collectAsStateWithLifecycle()
    val jobs by model.jobs.collectAsStateWithLifecycle()
    val databaseError by model.databaseError.collectAsStateWithLifecycle()
    val indexLoaded by model.indexLoaded.collectAsStateWithLifecycle()
    var confirmForget by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments(), model::select)
    val active = jobs.any { !it.state.isFinished }
    val jobState = jobs.firstOrNull { !it.state.isFinished }?.state ?: jobs.firstOrNull()?.state
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Media", "Choose photos or videos to index locally. Kano keeps metadata and a content hash, not a copy of your files.") }
        item { Text(if (indexLoaded) "$count / 100 selected documents · cloud access off" else "Loading local index…") }
        if (databaseError) item { Text("The local index could not be read. Reopen Kano to retry. No files have been deleted.") }
        item { Button(onClick = { picker.launch(arrayOf("image/*", "video/*")) }, enabled = !busy && !active) { Text("Choose photos or videos") } }
        item { OutlinedButton(onClick = model::scan, enabled = count > 0 && !busy && !active) { Text("Refresh index") } }
        if (active || busy) item {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(if (jobState == WorkInfo.State.RUNNING) "Indexing selected documents…" else "Waiting for work to finish or for sufficient battery and storage.")
        }
        if (active) item { TextButton(onClick = model::cancel, enabled = !busy) { Text("Stop indexing") } }
        if (jobState == WorkInfo.State.FAILED) item { Text("Indexing interrupted. Refresh index to retry unfinished documents.") }
        if (jobState == WorkInfo.State.CANCELLED) item { Text("Indexing stopped. Refresh index to resume.") }
        if (indexLoaded && !databaseError && count == 0) item { Text("No media selected. You control which files Kano can read. Original files are never deleted here.") }
        else if (count > 0) {
            item { OutlinedTextField(value = query, onValueChange = model::search, label = { Text("Search selected filenames") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            if (rows.isEmpty()) item { Text("No items on this page. Change your search or return to the previous page.") }
            items(rows, key = { it.uri }) { MediaRow(it) }
            item { Text("Page ${page + 1} · up to ${KanoViewModel.PAGE_SIZE} results per page") }
            item { OutlinedButton(onClick = { model.page.value-- }, enabled = page > 0) { Text("Previous page") } }
            item { OutlinedButton(onClick = { model.page.value++ }, enabled = rows.size == KanoViewModel.PAGE_SIZE) { Text("Next page") } }
            item { TextButton(onClick = { confirmForget = true }, enabled = !busy) { Text("Forget selected media index") } }
        }
        item { Text("This build indexes metadata and hashes files up to 100 MiB. OCR, semantic classification, duplicate review, and cleanup are planned.") }
    }
    if (confirmForget) AlertDialog(
        onDismissRequest = { confirmForget = false },
        title = { Text("Forget this index?") },
        text = { Text("Remove all indexed metadata and release Kano's saved read access. Your original photos and videos stay where they are.") },
        confirmButton = { TextButton(onClick = { confirmForget = false; model.forget() }) { Text("Forget index") } },
        dismissButton = { TextButton(onClick = { confirmForget = false }) { Text("Keep index") } },
    )
}

@Composable private fun MediaRow(record: MediaRecord) {
    val context = LocalContext.current
    val size = record.sizeBytes?.let { Formatter.formatFileSize(context, it) } ?: "Size unavailable"
    val state = when (record.state) {
        "QUEUED" -> "Waiting to index"
        "INDEXING" -> "Indexing; refresh to resume if interrupted"
        "INDEXED" -> "Metadata and SHA-256 indexed"
        "METADATA_ONLY" -> "Metadata only; file exceeds hash limit or size is unknown"
        "ACCESS_REVOKED" -> "Access lost; choose the document again"
        "UNSUPPORTED" -> "Not a supported image or video"
        else -> if (record.errorCode == "SOURCE_CHANGED") "File changed during reading; refresh to retry" else "Source unavailable; refresh or choose again"
    }
    FactRow(record.name, size, "$state. Meaning and sensitivity: not assessed.")
}
