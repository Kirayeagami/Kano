package app.kano.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
        item { SectionTitle("Personal Care", "Track products you own and avoid overspending.") }

        // Header & Product Actions Card (Inspired by Reference Images)
        item {
            KanoGlassCard(cornerRadius = 24.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Sanitizer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Grooming & Care Inventory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip("LOCAL INVENTORY")
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Record products you actively use. Kano prevents duplicate purchases by tracking stock status.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(14.dp))
                KanoHeroButton(
                    onClick = {
                        editingId = null; name = ""; category = ""; status = CareStatus.ACTIVE.name
                        model?.careError?.value = null; editing = true
                    },
                    enabled = model != null && state is CareState.Ready && !saving &&
                        (state as? CareState.Ready)?.items.orEmpty().size < 200,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("Add Product to Inventory", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        // Anti-Overspending Smart Check
        item {
            val itemCount = (state as? CareState.Ready)?.items.orEmpty().size
            KanoGlassCard(glassColor = KanoGreenContainer.copy(alpha = 0.88f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Anti-Overspending Smart Check",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip(if (itemCount > 0) "CHECK PASSED" else "EMPTY INVENTORY")
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (itemCount > 0) "NO PURCHASE NEEDED — You already own $itemCount active care products in your inventory."
                           else "NO ACTIVE PRODUCTS — Add your current products to enable anti-overspending checks.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Product Inventory List
        when (state) {
            CareState.Loading -> item { Text("Loading your inventory…") }
            CareState.Failed -> item { Text("Inventory could not be read. Reopen Kano to retry. No records were removed.") }
            is CareState.Ready -> {
                if (state.items.isEmpty()) item {
                    KanoGlassCard {
                        Text("No products saved yet.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Start by tapping 'Add Product to Inventory' above to track what you use.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                items(state.items, key = { it.id }) { product ->
                    KanoGlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (product.category.isNotBlank()) Text(product.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusChip(
                                text = CareStatus.entries.find { it.name == product.status }?.label ?: "Unknown",
                                containerColor = when (product.status) {
                                    "ACTIVE" -> MaterialTheme.colorScheme.primaryContainer
                                    "LOW", "NEARLY_EMPTY" -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                            )
                        }

                        Spacer(Modifier.height(8.dp))
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
