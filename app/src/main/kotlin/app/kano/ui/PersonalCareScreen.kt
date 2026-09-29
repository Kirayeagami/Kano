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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PersonalCareItem(
    val name: String,
    val category: String,
    val brand: String,
    val status: String,
    val detail: String,
)

@Composable
fun PersonalCareScreen() {
    val items = listOf(
        PersonalCareItem("Gentle Hydrating Cleanser", "Skincare", "CeraVe", "ACTIVE", "200ml · ~70% remaining"),
        PersonalCareItem("Daily Sunscreen SPF 50", "Skincare", "Neutrogena", "LOW", "50ml · ~15% remaining · Reorder soon"),
        PersonalCareItem("Nourishing Hair Shampoo", "Hair Care", "L'Oreal", "ACTIVE", "300ml · ~60% remaining"),
        PersonalCareItem("Precision Beard Trimmer", "Grooming", "Philips", "ACTIVE", "Battery 80% · Cleaned"),
        PersonalCareItem("Woody Eau De Parfum", "Fragrance", "Zara", "NEARLY EMPTY", "100ml · ~5% remaining"),
        PersonalCareItem("Mint Fluoride Toothpaste", "Oral Care", "Colgate", "ACTIVE", "150g · ~50% remaining"),
    )

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionTitle(
                title = "Personal Care & Lifestyle",
                detail = "Track personal care products, manage routines, and prevent duplicate or unnecessary purchases.",
            )
        }

        // Product Scanner & Add Actions Card
        item {
            KanoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Sanitizer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Add / Scan Personal Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = "Scan product label or upload photo to auto-detect category, brand, and usage status.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    KanoButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Scan", maxLines = 1)
                    }
                    KanoOutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Photo", maxLines = 1)
                    }
                    KanoOutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.UploadFile, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Upload", maxLines = 1)
                    }
                }
            }
        }

        // What Do I Need? Anti-Overspending Card
        item {
            KanoCard(backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Text(
                    text = "Inventory Need Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip("NO PURCHASE NEEDED", containerColor = Color(0xFF2E6B38), contentColor = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Cleanser & Shampoo in stock",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = "You already own 2 active skincare cleansers. No new facial cleanser required.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip("POTENTIAL GAP", containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Sunscreen SPF 50 is running low (~15%)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Personal Inventory List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "My Product Inventory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                StatusChip("${items.size} ITEMS")
            }
        }

        // Items List
        items(items, key = { it.name }) { item ->
            ProductInventoryCard(item)
        }
    }
}

@Composable
private fun ProductInventoryCard(item: PersonalCareItem) {
    val (chipBg, chipFg) = when (item.status) {
        "LOW", "NEARLY EMPTY" -> MaterialTheme.colorScheme.error to Color.White
        "ACTIVE" -> Color(0xFF2E6B38) to Color.White
        "EMPTY", "EXPIRED" -> MaterialTheme.colorScheme.outline to Color.White
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    KanoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${item.brand} · ${item.category}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            StatusChip(text = item.status, containerColor = chipBg, contentColor = chipFg)
        }
        Text(
            text = item.detail,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
