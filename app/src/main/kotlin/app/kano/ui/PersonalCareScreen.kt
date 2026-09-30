package app.kano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.data.*
import kotlinx.coroutines.flow.*

sealed interface CareState {
    data object Loading : CareState
    data object Failed : CareState
    data class Ready(val items: List<CareRecord>) : CareState
}
fun Flow<List<CareRecord>>.mapCareState(): Flow<CareState> =
    map<List<CareRecord>, CareState> { CareState.Ready(it) }.catch { emit(CareState.Failed) }

@Composable
fun PersonalCareScreen(model: KanoViewModel? = null, openCamera: (() -> Unit)? = null, footerInset: androidx.compose.ui.unit.Dp = 0.dp) {
    val state = model?.careItems?.collectAsStateWithLifecycle()?.value ?: CareState.Ready(emptyList())
    val saving = model?.careSaving?.collectAsStateWithLifecycle()?.value ?: false
    val error = model?.careError?.collectAsStateWithLifecycle()?.value
    var editing by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(CareStatus.UNKNOWN.name) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryFilter by rememberSaveable { mutableStateOf("All") }
    val records = (state as? CareState.Ready)?.items.orEmpty()
    val colors = KanoThemeColors
    LazyColumn(modifier = Modifier.testTag("care-list"), contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Personal Care", "Know what you have. Choose what you need.") }
        item {
            if (state is CareState.Ready) {
                Text("${records.size} saved · ${records.count { it.status == CareStatus.ACTIVE.name }} marked active · ${records.count { it.status in setOf(CareStatus.LOW.name, CareStatus.NEARLY_EMPTY.name) }} marked low", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                Text("Stock status is user entered. No purchase decision is inferred.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
        item {
            KanoHeroButton(onClick = {
                editingId = null; name = ""; category = ""; status = CareStatus.ACTIVE.name
                model?.careError?.value = null; editing = true
            }, enabled = model != null && state is CareState.Ready && !saving && records.size < 200) { Text("Add Product to Inventory") }
            Spacer(Modifier.height(8.dp))
            KanoOutlinedButton({ openCamera?.invoke() }, enabled = openCamera != null, modifier = Modifier.fillMaxWidth()) { Text("Read a product label with Kano Vision") }
            Text("Local text/QR only. Product identity and ingredients require your review.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary, modifier = Modifier.padding(top = 4.dp))
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Skincare", "Hair", "Grooming", "Oral", "Everyday").forEach { option ->
                    FilterChip(categoryFilter == option, { categoryFilter = option }, label = { Text(option) })
                }
            }
        }
        when (state) {
            CareState.Loading -> item { KanoStateSurface("Reading inventory", "Loading this device's saved records.", true) }
            CareState.Failed -> item { KanoStateSurface("Inventory unavailable", "Reopen Kano to retry. An empty inventory is not assumed.") }
            is CareState.Ready -> {
                val shown = records.filter { categoryFilter == "All" || it.category.equals(categoryFilter, true) }
                if (shown.isEmpty()) item { KanoStateSurface(if (records.isEmpty()) "No products saved yet." else "No products in this category", "Add only products you own. Change status when you review stock.") }
                items(shown, key = { it.id }) { product ->
                    KanoGlassCard {
                        Text(product.name, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                        if (product.category.isNotBlank()) Text(product.category, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                        StatusChip(CareStatus.entries.find { it.name == product.status }?.label ?: "Unknown")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                enabled = !saving,
                                onClick = {
                                    editingId = product.id; name = product.name; category = product.category
                                    status = product.status; model?.careError?.value = null; editing = true
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = colors.accent)
                            ) { Text("Edit") }
                            TextButton(
                                enabled = !saving,
                                onClick = { model?.careError?.value = null; deleteId = product.id },
                                colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                            ) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }

    if (editing) AlertDialog(
        onDismissRequest = { if (!saving) editing = false },
        title = { Text(if (editingId == null) "Add Product" else "Edit Product") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(120) },
                    label = { Text("Product Name") },
                    supportingText = { Text("Required · 120 characters maximum") },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it.take(60) },
                    label = { Text("Category (e.g. Cleanser, Moisturizer, Sunscreen)") },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Status", fontWeight = FontWeight.Bold)
                CareStatus.entries.forEach { option ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = status == option.name, onClick = { status = option.name }, enabled = !saving)
                        TextButton(onClick = { status = option.name }, enabled = !saving) { Text(option.label) }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && name.isNotBlank(),
                onClick = {
                    model?.saveCare(editingId, name, category, CareStatus.entries.find { it.name == status } ?: CareStatus.UNKNOWN) { editing = false }
                },
            ) { Text(if (saving) "Saving…" else "Save Product") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = { editing = false }) { Text("Cancel") } },
    )

    if (deleteId != null) AlertDialog(
        onDismissRequest = { if (!saving) deleteId = null },
        title = { Text("Delete Product?") },
        text = { Column { Text("Remove this product record from Kano. This cannot be undone."); error?.let { Text(it) } } },
        confirmButton = { TextButton(enabled = !saving, onClick = { deleteId?.let { model?.deleteCare(it) { deleteId = null } } }) { Text("Delete Product") } },
        dismissButton = { TextButton(enabled = !saving, onClick = { deleteId = null }) { Text("Keep Product") } },
    )
}

