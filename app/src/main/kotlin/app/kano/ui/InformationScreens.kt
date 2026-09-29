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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kano.BuildConfig

@Composable
fun HomeScreen(
    openDevice: () -> Unit,
    openMedia: () -> Unit,
    openStyle: () -> Unit,
    openPersonalCare: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionTitle(
                title = "Your device.\nYour decisions.",
                detail = "Kano · foundation build ${BuildConfig.VERSION_NAME}",
            )
        }

        // Hero Device Summary Card
        item {
            KanoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Device Health & Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip("SNAPSHOT")
                }
                Text(
                    text = "Storage: ~72% used · Memory: System snapshot · Battery: Normal",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                KanoButton(
                    onClick = openDevice,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("View detailed device usage")
                }
            }
        }

        // Media Intelligence Summary Card
        item {
            KanoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.PermMedia,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Media & Documents Index",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip("LOCAL ONLY")
                }
                Text(
                    text = "Index selected photos or videos. Kano hashes content locally without copying your files.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                KanoOutlinedButton(
                    onClick = openMedia,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Choose photos or videos to index")
                }
            }
        }

        // Style Studio & Personal Care Shortcut Card
        item {
            KanoCard(backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Checkroom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Style & Personal Care",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip("RECOMMENDED")
                }
                Text(
                    text = "Today: Dark Wine Overshirt + Grey Trousers · 2 Personal Care items low stock.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KanoButton(
                        onClick = openStyle,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Style Studio")
                    }
                    KanoOutlinedButton(
                        onClick = openPersonalCare,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Personal Care")
                    }
                }
            }
        }

        // Security & Privacy Boundary Card
        item {
            KanoCard(backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "100% Local Privacy Boundary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "No Internet permission · No cloud upload · Keystore encrypted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SectionTitle("Privacy & roadmap", "Access is scoped to what you choose.") }

        item {
            KanoCard {
                FactRow("Cloud AI", "Off · not integrated", "OpenAI, Gemini, Perplexity and local inference are planned. No API key is requested in this build.")
                FactRow("Media access", "Selected documents only", "Use Media → Forget selected media index to remove saved metadata and release read grants.")
                FactRow("Storage", "On this device", "App-private metadata database. No additional database encryption. Backup and transfer are excluded; uninstalling removes the index.")
                FactRow("Other access", "Not requested", "No notification listener, Gmail, contacts, location, broad storage, usage access or accessibility service.")
            }
        }

        item {
            KanoCard(backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Text("Version & Release Roadmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FactRow("Installed version", BuildConfig.VERSION_NAME, "No update service is connected. Available version and release date are unknown.")
                FactRow("1.0 · In development", "Device + Media", "Indexing → OCR / QR / URLs → knowledge extraction → reviewed cleanup.")
                FactRow("1.1 · Planned", "Gmail + notifications")
                FactRow("1.2–1.4 · Planned", "Style, shopping, research", "1.2 Style Lab · 1.3 Shopping intelligence · 1.4 Perplexity research")
                FactRow("2.0–3.0 · Planned", "Knowledge and local AI", "2.0 Knowledge graph · 2.1 Android capabilities · 3.0 Local AI improvements")
            }
        }
    }
}
