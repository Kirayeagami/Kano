package app.kano.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kano.BuildConfig

@Composable
fun HomeScreen(
    openDevice: () -> Unit,
    openMedia: () -> Unit,
    openStyle: () -> Unit,
    openPersonalCare: () -> Unit,
) {
    var isAssembled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isAssembled = true }

    Box(modifier = Modifier.fillMaxSize()) {
        KanoWaveBackground()

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Greeting & Oversized Display Heading
            item {
                AnimatedVisibility(
                    visible = isAssembled,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 4 },
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(
                            text = "Kano Assistant",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Your day,\nat a glance.",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = "Local-first personal intelligence · Build ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }

            // Hero Focus Card: "3 things need your attention" with Coral Gradient & Glass Overlay
            item {
                AnimatedVisibility(
                    visible = isAssembled,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
                ) {
                    KanoGradientHero(cornerRadius = 24.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "3 Things Need Your Attention",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                            )
                            StatusChip("ACTION NEEDED", containerColor = Color.White, contentColor = KanoCoralGradientEnd)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "• Storage volume is ~72% full. Review large media files.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                        Text(
                            text = "• Sunscreen SPF 50 is running low (~15% remaining).",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = "• Selected media index has unindexed items queued.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        KanoGlassSurface(
                            cornerRadius = 16.dp,
                            glassColor = Color.White.copy(alpha = 0.25f),
                            borderColor = Color.White.copy(alpha = 0.4f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Resolve items now",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                )
                                KanoFloatingControl(
                                    icon = Icons.Outlined.ChevronRight,
                                    contentDescription = "Resolve",
                                    onClick = openDevice,
                                    size = 40.dp,
                                    containerColor = Color.White,
                                    contentColor = KanoCoralGradientEnd,
                                )
                            }
                        }
                    }
                }
            }

            // Section Overview Cards
            item {
                Text(
                    text = "Intelligence Sections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // Device Intelligence Snapshot Card
            item {
                KanoGlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Smartphone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Device Health & Storage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip("SNAPSHOT")
                    }
                    Text(
                        text = "Storage: ~72% used · RAM: System snapshot · Battery: Charging",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    KanoOutlinedButton(
                        onClick = openDevice,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("View device analytics")
                    }
                }
            }

            // Media & Documents Index Card
            item {
                KanoGlassCard {
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
                        text = "Choose photos or videos to index. Kano calculates local SHA-256 hashes without copying your files.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    KanoOutlinedButton(
                        onClick = openMedia,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Manage media index")
                    }
                }
            }

            // Style Studio & Personal Care Entry Card
            item {
                KanoGlassCard(glassColor = KanoLavenderContainer.copy(alpha = 0.85f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Checkroom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Style Studio & Personal Care",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip("RECOMMENDED")
                    }
                    Text(
                        text = "Today: Dark Wine Overshirt + Grey Trousers · 2 Care items low stock.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
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

            // Privacy Guarantee Badge Card
            item {
                KanoGlassCard(cornerRadius = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "100% Local Privacy Guarantee",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Zero Internet permission · Local Room DB · Keystore AES-256 encrypted",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    KanoOutlinedButton(
                        onClick = { model?.setThemeMode(KanoThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.SettingsSuggest, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("System", maxLines = 1, fontWeight = if (currentThemeMode == KanoThemeMode.SYSTEM) FontWeight.Bold else FontWeight.Normal)
                    }
                    KanoOutlinedButton(
                        onClick = { model?.setThemeMode(KanoThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.LightMode, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Light", maxLines = 1, fontWeight = if (currentThemeMode == KanoThemeMode.LIGHT) FontWeight.Bold else FontWeight.Normal)
                    }
                    KanoOutlinedButton(
                        onClick = { model?.setThemeMode(KanoThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.DarkMode, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Dark", maxLines = 1, fontWeight = if (currentThemeMode == KanoThemeMode.DARK) FontWeight.Bold else FontWeight.Normal)
                    }
                }
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
                FactRow("1.0 · In Development", "Device + Media", "Indexing → OCR / QR / URLs → knowledge extraction → reviewed cleanup.")
                FactRow("1.1 · Planned", "Gmail + Notifications")
                FactRow("1.2–1.4 · Planned", "Style, Shopping, Research", "1.2 Style Lab · 1.3 Shopping intelligence · 1.4 Perplexity research")
                FactRow("2.0–3.0 · Planned", "Knowledge Graph & Local AI", "2.0 Knowledge graph · 2.1 Android capabilities · 3.0 Local AI improvements")
            }
        }
    }
}
