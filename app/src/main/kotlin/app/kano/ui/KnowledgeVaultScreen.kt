package app.kano.ui

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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.data.KnowledgeRecord

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KnowledgeVaultScreen(model: KanoViewModel? = null) {
    val entities = model?.knowledgeEntities?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val totalCount = model?.knowledgeCount?.collectAsStateWithLifecycle()?.value ?: 0
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredEntities = entities.filter { record ->
        val matchesCategory = when (selectedCategory) {
            "WEBSITE" -> record.entityType == "WEBSITE"
            "STUDY" -> record.entityType == "STUDY_NOTE"
            "MOVIE" -> record.entityType == "MOVIE"
            "RECEIPT" -> record.entityType == "RECEIPT"
            "QR" -> record.entityType == "QR_PAYLOAD"
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() ||
            record.title.contains(searchQuery, ignoreCase = true) ||
            record.detail.contains(searchQuery, ignoreCase = true) ||
            (record.urlOrPayload?.contains(searchQuery, ignoreCase = true) == true)

        matchesCategory && matchesQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        KanoWaveBackground(waveColor = KanoPeachContainer.copy(alpha = 0.35f))

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionTitle(
                    title = "Knowledge Vault",
                    detail = "Structured knowledge extracted from your local photos, screenshots, and QR codes.",
                )
            }

            // Vault Overview Summary Card
            item {
                KanoGlassCard(cornerRadius = 24.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Stored Knowledge ($totalCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip("LOCAL VAULT")
                    }
                    Text(
                        text = "Every knowledge entity remembers its source document provenance and local extraction method.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Category Filter Badges Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChipButton("ALL", selectedCategory == "ALL") { selectedCategory = "ALL" }
                        FilterChipButton("WEBSITES", selectedCategory == "WEBSITE") { selectedCategory = "WEBSITE" }
                        FilterChipButton("STUDY", selectedCategory == "STUDY") { selectedCategory = "STUDY" }
                        FilterChipButton("MOVIES", selectedCategory == "MOVIE") { selectedCategory = "MOVIE" }
                        FilterChipButton("RECEIPTS", selectedCategory == "RECEIPT") { selectedCategory = "RECEIPT" }
                        FilterChipButton("QR CODES", selectedCategory == "QR") { selectedCategory = "QR" }
                    }
                }
            }

            // Search Bar
            if (totalCount > 0) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search stored knowledge") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Outlined.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Knowledge List Items
            if (filteredEntities.isEmpty()) {
                item {
                    KanoGlassCard {
                        Text(
                            text = if (totalCount == 0) "Knowledge Vault is empty" else "No matching knowledge entities found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (totalCount == 0) "Scan indexed media documents using Media → Local OCR & QR Scan to extract structured knowledge." else "Try changing your search query or filter criteria.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(filteredEntities, key = { it.id }) { record ->
                    VaultEntityCard(record = record, onDelete = { model?.deleteKnowledgeEntity(it) })
                }
            }
        }
    }
}

@Composable
private fun FilterChipButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun VaultEntityCard(record: KnowledgeRecord, onDelete: (String) -> Unit) {
    KanoGlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Type: ${record.entityType} · Source: Local ${record.extractionType}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = { onDelete(record.id) }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete Entity")
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text = record.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!record.urlOrPayload.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = record.urlOrPayload,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = "Open Link",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = "Provenance: Document ${record.sourceUri.takeLast(30)} · ${record.confidence}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
