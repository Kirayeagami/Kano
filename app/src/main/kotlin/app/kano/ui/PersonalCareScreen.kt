package app.kano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.data.CareRecord
import app.kano.data.CareStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed interface CareState {
    data object Loading : CareState
    data object Failed : CareState
    data class Ready(val items: List<CareRecord>) : CareState
}

fun Flow<List<CareRecord>>.mapCareState(): Flow<CareState> =
    map<List<CareRecord>, CareState> { CareState.Ready(it) }.catch { emit(CareState.Failed) }

@Composable
fun PersonalCareScreen(model: KanoViewModel? = null) {
    val state = model?.careItems?.collectAsStateWithLifecycle()?.value ?: CareState.Ready(emptyList())
    val saving = model?.careSaving?.collectAsStateWithLifecycle()?.value ?: false
    val error = model?.careError?.collectAsStateWithLifecycle()?.value
    var editing by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(CareStatus.UNKNOWN.name) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Personal Care", "Your products, recorded by you. Saved only on this device.") }
        item {
            KanoGlassCard {
                Text("Keep track of what you own", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Add a name, category and status. Quantities, expiry dates and purchase needs are never guessed.")
                Spacer(Modifier.height(12.dp))
                KanoButton(onClick = {
                    editingId = null; name = ""; category = ""; status = CareStatus.UNKNOWN.name
                    model?.careError?.value = null; editing = true
                }, enabled = model != null && state is CareState.Ready && !saving &&
                    (state as? CareState.Ready)?.items.orEmpty().size < 200) { Text("Add product") }
                Text("Manual entry · Up to 200 products · Scanning unavailable", style = MaterialTheme.typography.bodySmall)
            }
        }
        when (state) {
            CareState.Loading -> item { Text("Loading your inventory…") }
            CareState.Failed -> item { Text("Inventory could not be read. Reopen Kano to retry. No records were removed.") }
            is CareState.Ready -> {
                if (state.items.isEmpty()) item {
                    KanoGlassCard {
                        Text("No products saved", style = MaterialTheme.typography.titleMedium)
                        Text("Start with a product you already use. There is no sample inventory.")
                    }
                }
                items(state.items, key = { it.id }) { product ->
                    KanoGlassCard {
                        Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (product.category.isNotBlank()) Text(product.category)
                        Text("Status: " + (CareStatus.entries.find { it.name == product.status }?.label ?: "Unknown"))
                        Text("Entered by you", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(enabled = !saving, onClick = {
                                editingId = product.id; name = product.name; category = product.category
                                status = product.status; model?.careError?.value = null; editing = true
                            }) { Text("Edit") }
                            TextButton(enabled = !saving, onClick = { model?.careError?.value = null; deleteId = product.id }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }

    if (editing) AlertDialog(
        onDismissRequest = { if (!saving) editing = false },
        title = { Text(if (editingId == null) "Add product" else "Edit product") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(120) }, label = { Text("Product name") },
                    supportingText = { Text("Required · 120 characters maximum") }, enabled = !saving, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it.take(60) }, label = { Text("Category (optional)") },
                    enabled = !saving, modifier = Modifier.fillMaxWidth())
                Text("Status · your assessment", fontWeight = FontWeight.Bold)
                CareStatus.entries.forEach { option ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = status == option.name, onClick = { status = option.name }, enabled = !saving)
                        TextButton(onClick = { status = option.name }, enabled = !saving) { Text(option.label) }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving && name.isNotBlank(), onClick = {
                model?.saveCare(editingId, name, category, CareStatus.entries.find { it.name == status } ?: CareStatus.UNKNOWN) { editing = false }
            }) { Text(if (saving) "Saving…" else "Save product") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = { editing = false }) { Text("Cancel") } },
    )

    if (deleteId != null) AlertDialog(
        onDismissRequest = { if (!saving) deleteId = null },
        title = { Text("Delete saved product?") },
        text = { Column { Text("Remove this manually entered record from Kano. This cannot be undone."); error?.let { Text(it) } } },
        confirmButton = { TextButton(enabled = !saving, onClick = { deleteId?.let { model?.deleteCare(it) { deleteId = null } } }) { Text("Delete product") } },
        dismissButton = { TextButton(enabled = !saving, onClick = { deleteId = null }) { Text("Keep product") } },
    )
}
