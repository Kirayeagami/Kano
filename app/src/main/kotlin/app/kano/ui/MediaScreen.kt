package app.kano.ui

import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo

/** Optional SAF utility; the primary Media route is the MediaStore gallery. */
@Composable
fun MediaScreen(model: KanoViewModel, footerInset: androidx.compose.ui.unit.Dp = 0.dp) {
    val rows by model.rows.collectAsStateWithLifecycle()
    val count by model.count.collectAsStateWithLifecycle()
    val query by model.query.collectAsStateWithLifecycle()
    val page by model.page.collectAsStateWithLifecycle()
    val busy by model.busy.collectAsStateWithLifecycle()
    val jobs by model.jobs.collectAsStateWithLifecycle()
    val failed by model.databaseError.collectAsStateWithLifecycle()
    val loaded by model.indexLoaded.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments(), model::select)
    val active = jobs.any { !it.state.isFinished }
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Saved documents", "An optional index for explicitly chosen files.") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (failed) "Index unavailable" else if (loaded) "$count Selected Documents" else "Reading index…", style = MaterialTheme.typography.titleMedium, color = KanoThemeColors.textPrimary)
                Text("Limit 100 · original files are retained", style = MaterialTheme.typography.bodySmall, color = KanoThemeColors.textSecondary)
                Spacer(Modifier.height(4.dp))
                KanoHeroButton({ picker.launch(arrayOf("image/*", "video/*")) }, enabled = !busy && !active) { Text("Choose photos or videos") }
                KanoOutlinedButton(model::scan, enabled = count > 0 && !busy && !active) { Text("Refresh index") }
            }
        }
        if (active || busy) item {
            KanoStateSurface(if (jobs.any { it.state == WorkInfo.State.RUNNING }) "Indexing selected documents" else "Waiting for work", "Actual background-job state; no estimated progress.", true)
            if (active) TextButton(model::cancel, enabled = !busy) { Text("Stop indexing") }
        }
        if (failed) item { KanoStateSurface("Index read failed", "Reopen Kano to retry.") }
        if (count > 0) {
            item { OutlinedTextField(query, model::search, modifier = Modifier.fillMaxWidth(), label = { Text("Search selected filenames") }, singleLine = true, shape = MaterialTheme.shapes.large) }
            if (rows.isEmpty()) item { Text("No items match your search on this page.", color = KanoThemeColors.textSecondary) }
            items(rows, key = { it.uri }) { record ->
                KanoGlassCard {
                    Text(record.name, style = MaterialTheme.typography.titleMedium, color = KanoThemeColors.textPrimary)
                    Text(record.sizeBytes?.let { Formatter.formatFileSize(context, it) } ?: "Size unavailable", style = MaterialTheme.typography.bodySmall, color = KanoThemeColors.textSecondary)
                    StatusChip(when (record.state) { "ERROR" -> "ERROR · RETRY INDEX"; "ACCESS_REVOKED" -> "REVOKED"; else -> record.state.replace('_', ' ') })
                    KanoOutlinedButton({ model.runMediaOcr(record) }, enabled = !busy && record.state != "ACCESS_REVOKED" && record.mime?.startsWith("image/") != false) { Text("Local OCR & QR Scan") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KanoOutlinedButton({ model.page.value-- }, enabled = page > 0) { Text("Previous") }
                    KanoOutlinedButton({ model.page.value++ }, enabled = rows.size == KanoViewModel.PAGE_SIZE) { Text("Next") }
                }
                TextButton({ confirm = true }, enabled = !busy) { Text("Forget selected media index") }
            }
        } else if (loaded && !failed) item { KanoStateSurface("No media selected yet.", "The main Media tab is your gallery. This index is for selected documents only.") }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Forget this index?") },
        text = { Text("Remove indexed metadata and release saved read access. Original files and reviewed Vault records are kept.") },
        confirmButton = { TextButton({ confirm = false; model.forget() }) { Text("Forget index") } },
        dismissButton = { TextButton({ confirm = false }) { Text("Keep index") } })
}
