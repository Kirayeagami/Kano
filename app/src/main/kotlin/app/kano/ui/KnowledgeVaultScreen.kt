package app.kano.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.core.VisionPolicy
import app.kano.data.KnowledgeRecord
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeVaultScreen(model: KanoViewModel? = null, footerInset: androidx.compose.ui.unit.Dp = 0.dp) {
    val records = model?.knowledgeEntities?.collectAsStateWithLifecycle()?.value.orEmpty()
    val loaded = model?.knowledgeLoaded?.collectAsStateWithLifecycle()?.value ?: true
    val failed = model?.knowledgeError?.collectAsStateWithLifecycle()?.value ?: false
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    var selected by remember { mutableStateOf<KnowledgeRecord?>(null) }
    var confirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val shown = records.filter { (filter == "All" || it.entityType == filter) && (query.isBlank() || (it.title + " " + it.detail + " " + it.urlOrPayload.orEmpty()).contains(query, true)) }
    fun open(uri: String, source: Boolean) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply { if (source) addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }); error = null }
        catch (_: Exception) { error = "Could not open this item. Its app or source access may be unavailable." }
    }
    val colors = KanoThemeColors
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Knowledge Vault", "A memory with a source.") }
        item { Text(if (failed) "Vault unavailable" else if (!loaded) "Reading saved memory…" else "${records.size} saved records · on this device", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary) }
        item { OutlinedTextField(query, { query = it.take(160) }, modifier = Modifier.fillMaxWidth(), label = { Text("Search stored knowledge") }, singleLine = true, shape = MaterialTheme.shapes.large) }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf("All") + records.map { it.entityType }.distinct()).forEach { type -> FilterChip(filter == type, { filter = type }, label = { Text(type.replace('_', ' ')) }) }
            }
        }
        if (failed) item { KanoStateSurface("Could not read the Vault", "Reopen Kano to retry. An empty database is not assumed.") }
        else if (!loaded) item { KanoStateSurface("Reading memory", "Loading saved records.", true) }
        else if (shown.isEmpty()) item { KanoStateSurface(if (records.isEmpty()) "Knowledge Vault is empty" else "No matching records", "Analyze an image in Media or Kano Vision, then explicitly save a reviewed result.") }
        else items(shown, key = { it.id }) { record -> KanoActionRow(record.title, record.entityType.replace('_', ' ') + " · " + record.extractionType + " · " + record.confidence, { selected = record; error = null }) }
    }
    selected?.let { record ->
        ModalBottomSheet(onDismissRequest = { selected = null }, containerColor = colors.surfaceElevated) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(record.title, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                StatusChip(record.confidence + " · " + record.extractionType)
                Text(record.detail, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
                record.urlOrPayload?.let { url ->
                    Text(url, style = MaterialTheme.typography.bodyMedium, color = colors.accent)
                    if (VisionPolicy.publicWebUrl(url) != null) KanoOutlinedButton({ open(url, false) }) { Text("Open reviewed website") }
                }
                FactRow("Source", record.sourceUri, "Access to the original image can be revoked.")
                FactRow("Saved", DateFormat.getDateTimeInstance().format(Date(record.createdAt)))
                KanoOutlinedButton({ open(record.sourceUri, true) }) { Text("Open source image") }
                TextButton({ confirm = true }, colors = ButtonDefaults.textButtonColors(contentColor = colors.error)) { Text("Delete record") }
                error?.let { Text(it, color = colors.error) }
            }
        }
        if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Delete this Vault record?") },
            text = { Text(record.title + "\nOnly the saved record is removed. Its original image is kept.") },
            confirmButton = { TextButton({ confirm = false; selected = null; model?.deleteKnowledgeEntity(record.id) }) { Text("Delete record") } },
            dismissButton = { TextButton({ confirm = false }) { Text("Keep") } })
    }
}
