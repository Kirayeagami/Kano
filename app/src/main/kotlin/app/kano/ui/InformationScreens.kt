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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@Composable
fun HomeScreen(openDevice: () -> Unit, openMedia: () -> Unit, openStyle: () -> Unit, openPersonalCare: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        KanoWaveBackground()
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("KANO / ON YOUR DEVICE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                SectionTitle("Make room for\nwhat matters.", "Your device, selected media and the products you choose to record.")
            }
            item {
                KanoGradientHero(startColor = MaterialTheme.colorScheme.primaryContainer, endColor = MaterialTheme.colorScheme.surface) {
                    Text("Start with what is real", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("See a measured snapshot of storage, memory and battery. No optimizer scores or guessed alerts.")
                    Spacer(Modifier.height(16.dp))
                    KanoButton(onClick = openDevice, modifier = Modifier.fillMaxWidth()) { Text("View device readings") }
                }
            }
            item {
                KanoGlassCard {
                    Text("Your media. Your choice.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Select photos or videos for a local metadata and hash index. OCR, semantic categories and cleanup are not available yet.")
                    Spacer(Modifier.height(12.dp))
                    KanoOutlinedButton(onClick = openMedia, modifier = Modifier.fillMaxWidth()) { Text("Manage media index") }
                }
            }
            item {
                KanoGlassCard {
                    Text("Use what you own", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Keep a personal care inventory using your own entries and status updates.")
                    Spacer(Modifier.height(12.dp))
                    KanoOutlinedButton(onClick = openPersonalCare, modifier = Modifier.fillMaxWidth()) { Text("Open Personal Care") }
                    KanoOutlinedButton(onClick = openStyle, modifier = Modifier.fillMaxWidth()) { Text("Style Studio availability") }
                }
            }
            item {
                KanoGlassCard {
                    Text("Local by design", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("No Internet permission or connected AI services. The metadata and inventory database is app-private, without extra database encryption.")
                    Text("Development build ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
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
                    androidx.compose.material3.Switch(checked = glass, onCheckedChange = { model?.setGlass(it) },
                        modifier = Modifier.semantics { contentDescription = "Glass surfaces" })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reduce motion", modifier = Modifier.weight(1f))
                    androidx.compose.material3.Switch(checked = reducedMotion, onCheckedChange = { model?.setReducedMotion(it) },
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
                FactRow("1.0 · In Development", "Device + Media", "Indexing → OCR / QR / URLs → knowledge extraction → reviewed cleanup.")
                FactRow("1.1 · Planned", "Gmail + Notifications")
                FactRow("1.2–1.4 · Planned", "Style, Shopping, Research", "1.2 Style Lab · 1.3 Shopping intelligence · 1.4 Perplexity research")
                FactRow("2.0–3.0 · Planned", "Knowledge Graph & Local AI", "2.0 Knowledge graph · 2.1 Android capabilities · 3.0 Local AI improvements")
            }
        }
    }
}