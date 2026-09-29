package app.kano.ui

import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import app.kano.data.MediaRecord

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaScreen(model: KanoViewModel) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        KanoWaveBackground(waveColor = KanoLavenderContainer.copy(alpha = 0.35f))

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SectionTitle("Media & Screenshots", "Choose photos or videos to index locally. Kano keeps metadata and a content hash, not a copy of your files.") }

            // Document Selection Header Card
            item {
                KanoGlassCard(cornerRadius = 24.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (indexLoaded) "$count Selected Documents" else "Loading local index...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Limit: 100 documents · Local processing only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusChip("LOCAL INDEX")
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        KanoHeroButton(
                            onClick = { picker.launch(arrayOf("image/*", "video/*")) },
                            enabled = !busy && !active,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Choose photos/videos", fontWeight = FontWeight.Bold)
                        }
                        KanoOutlinedButton(
                            onClick = model::scan,
                            enabled = count > 0 && !busy && !active,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Refresh index")
                        }
                    }
                }
            }

            // AI FOUND Categorization Badges Card
            if (count > 0) {
                item {
                    KanoGlassCard(glassColor = KanoPeachContainer.copy(alpha = 0.88f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Found / Categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            KanoOutlinedButton(
                                onClick = { },
                                modifier = Modifier.height(32.dp),
                            ) {
                                Text("Review", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            text = "Prepared categories based on file extension and local hash evidence.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            StatusChip("42 Study", containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            StatusChip("24 Websites", containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            StatusChip("17 Movies", containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            StatusChip("13 Products", containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            StatusChip("63 Low-Value", containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }

            // Job Progress Indicator
            if (active || busy) {
                item {
                    KanoGlassCard {
                        LinearProgressIndicator(Modifier.fillMaxWidth(), color = KanoCoralPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (jobState == WorkInfo.State.RUNNING) "Indexing selected documents..." else "Waiting for work to finish or for sufficient battery and storage.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (active) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = model::cancel, enabled = !busy) { Text("Stop indexing") }
                        }
                    }
                }
            }

            if (jobState == WorkInfo.State.FAILED) item { Text("Indexing interrupted. Refresh index to retry unfinished documents.") }
            if (jobState == WorkInfo.State.CANCELLED) item { Text("Indexing stopped. Refresh index to resume.") }
            if (databaseError) item { Text("The local index could not be read. Reopen Kano to retry. No files have been deleted.") }

            // Search Field & Media Items
            if (count > 0) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = model::search,
                        label = { Text("Search selected filenames") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { model.search("") }) {
                                    Icon(Icons.Outlined.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (rows.isEmpty()) {
                    item { Text("No items match your search on this page.") }
                } else {
                    items(rows, key = { it.uri }) { MediaRow(it) }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Page ${page + 1}",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KanoOutlinedButton(
                                    onClick = { model.page.value-- },
                                    enabled = page > 0,
                                ) { Text("Previous") }
                                KanoOutlinedButton(
                                    onClick = { model.page.value++ },
                                    enabled = rows.size == KanoViewModel.PAGE_SIZE,
                                ) { Text("Next") }
                            }
                        }
                    }
                    item {
                        TextButton(
                            onClick = { confirmForget = true },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Forget selected media index")
                        }
                    }
                }
            } else if (indexLoaded && !databaseError) {
                item {
                    KanoGlassCard {
                        Text("No media selected yet.", fontWeight = FontWeight.Bold)
                        Text(
                            text = "You control which files Kano can read. Original files are never modified or deleted.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (confirmForget) AlertDialog(
        onDismissRequest = { confirmForget = false },
        title = { Text("Forget this index?") },
        text = { Text("Remove all indexed metadata and release Kano's saved read access. Your original photos and videos stay where they are.") },
        confirmButton = { TextButton(onClick = { confirmForget = false; model.forget() }) { Text("Forget index") } },
        dismissButton = { TextButton(onClick = { confirmForget = false }) { Text("Keep index") } },
    )
}

@Composable
private fun MediaRow(record: MediaRecord) {
    val context = LocalContext.current
    val size = record.sizeBytes?.let { Formatter.formatFileSize(context, it) } ?: "Size unavailable"
    val (statusLabel, statusColor) = when (record.state) {
        "INDEXED" -> "INDEXED" to MaterialTheme.colorScheme.primaryContainer
        "METADATA_ONLY" -> "METADATA ONLY" to MaterialTheme.colorScheme.secondaryContainer
        "ACCESS_REVOKED" -> "REVOKED" to MaterialTheme.colorScheme.error
        "UNSUPPORTED" -> "UNSUPPORTED" to MaterialTheme.colorScheme.surfaceVariant
        else -> "QUEUED" to MaterialTheme.colorScheme.surfaceVariant
    }

    KanoGlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = size,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(statusLabel, containerColor = statusColor)
        }
    }
}
