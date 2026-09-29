package app.kano.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Sanitizer
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.BuildConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    openDevice: () -> Unit,
    openMedia: () -> Unit,
    openStyle: () -> Unit,
    openPersonalCare: () -> Unit,
    openVault: () -> Unit = {},
) {
    var queryText by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        KanoWaveBackground()

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // User Header Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Good evening, Kiray.",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Two things need you.",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "K",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            // Rounded Query Input Bar (Inspired by Reference Images)
            item {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Ask Kano or describe a task...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        Row(modifier = Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera Scan", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Outlined.Mic, contentDescription = "Voice Input", modifier = Modifier.size(20.dp))
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Hero Brief Card
            item {
                KanoGlassCard(cornerRadius = 24.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "HOME BRIEF",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        StatusChip("1 of 3 resolved")
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Device storage is at 72% capacity and 2 personal care items are running low (~15%).",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KanoHeroButton(
                            onClick = openDevice,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Resolve Now", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        KanoOutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Not now")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "💡 Local-first intelligence · Zero cloud data egress",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            // Services & Intelligence Category Grid
            item {
                Text(
                    text = "Personal Assistant Services",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CategoryTile("Device", Icons.Outlined.Smartphone, KanoBlueContainer, openDevice)
                    CategoryTile("Media", Icons.Outlined.PermMedia, KanoGreenContainer, openMedia)
                    CategoryTile("Vault", Icons.Outlined.Lightbulb, KanoPeachContainer, openVault)
                    CategoryTile("Style", Icons.Outlined.Checkroom, KanoLavenderContainer, openStyle)
                    CategoryTile("Care", Icons.Outlined.Sanitizer, KanoPeachContainer, openPersonalCare)
                }
            }

            // Local Sandbox Boundary Note
            item {
                KanoGlassCard {
                    Text("100% Local Privacy Boundary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Zero Internet permission. Your files, media index, and personal inventory remain on this device.", style = MaterialTheme.typography.bodySmall)
                    Text("Build ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier.width(105.dp).height(90.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color(0xFF111111), modifier = Modifier.size(24.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111),
            )
        }
    }
}

@Composable
fun SettingsScreen(model: KanoViewModel? = null) {
    val currentThemeMode = model?.themeMode?.collectAsStateWithLifecycle()?.value ?: KanoThemeMode.SYSTEM

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionTitle("Privacy & Appearance", "Access is strictly scoped to what you choose.") }

        // Appearance Theme Selector Card
        item {
            KanoGlassCard {
                Text(
                    text = "Appearance Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Select Bright Mode, Dark Mode, or follow System Default.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KanoThemeMode.entries.forEach { mode ->
                        KanoOutlinedButton(onClick = { model?.setThemeMode(mode) }, modifier = Modifier.fillMaxWidth()) {
                            Text((if (mode == currentThemeMode) "Selected · " else "") + mode.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
                val glass = model?.glass?.collectAsStateWithLifecycle()?.value ?: true
                val reducedMotion = model?.reducedMotion?.collectAsStateWithLifecycle()?.value ?: false
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Glass surfaces", modifier = Modifier.weight(1f))
                    Switch(checked = glass, onCheckedChange = { model?.setGlass(it) },
                        modifier = Modifier.semantics { contentDescription = "Glass surfaces" })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reduce motion", modifier = Modifier.weight(1f))
                    Switch(checked = reducedMotion, onCheckedChange = { model?.setReducedMotion(it) },
                        modifier = Modifier.semantics { contentDescription = "Reduce motion" })
                }
                Text("Glass changes surface transparency. Reduce motion stops ambient waves. Android's disabled animations are also respected.", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            KanoGlassCard {
                FactRow("Cloud AI", "Off · Not Integrated", "OpenAI, Gemini, Perplexity and local inference are planned. No API key is requested in this build.")
                FactRow("Media Access", "Selected Documents Only", "Use Media → Forget selected media index to remove saved metadata and release read grants.")
                FactRow("Storage", "On This Device", "App-private metadata database. No additional database encryption. Backup and transfer are excluded; uninstalling removes the index.")
                FactRow("Other Access", "Not Requested", "No notification listener, Gmail, contacts, location, broad storage, usage access or accessibility service.")
            }
        }

        item {
            KanoGlassCard(glassColor = KanoPeachContainer.copy(alpha = 0.85f)) {
                Text("Version & Release Roadmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FactRow("Installed Version", BuildConfig.VERSION_NAME, "No update service is connected. Available version and release date are unknown.")
                FactRow("1.0 · Complete", "Device + Media + Vault", "Indexing → OCR / QR / URLs → Knowledge Vault → safe cleanup.")
                FactRow("1.1 · Planned", "Gmail + Notifications")
                FactRow("1.2–1.4 · Planned", "Style, Shopping, Research", "1.2 Style Lab · 1.3 Shopping intelligence · 1.4 Perplexity research")
                FactRow("2.0–3.0 · Planned", "Knowledge Graph & Local AI", "2.0 Knowledge graph · 2.1 Android capabilities · 3.0 Local AI improvements")
            }
        }
    }
}
