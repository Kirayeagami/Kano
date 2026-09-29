package app.kano.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import app.kano.ui.KanoButton as Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.kano.BuildConfig

@Composable fun HomeScreen(openDevice: () -> Unit, openMedia: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Your device.\nYour decisions.", "Kano · foundation build ${BuildConfig.VERSION_NAME}") }
        item { Text("Start with what is on your device. Read current storage, memory, and battery information, or choose media for a private local index.") }
        item { Button(onClick = openDevice) { Text("View device readings") } }
        item { Button(onClick = openMedia) { Text("Choose media to index") } }
        item { FactRow("Data boundary", "Local processing", "This build has no Internet permission, connected accounts, or cloud AI providers.") }
        item { FactRow("User control", "Select. Review. Forget.", "Nothing is scanned until you choose it. Indexing never deletes original files.") }
        item { FactRow("Development status", "Foundation checkpoint", "Device readings and selected-media metadata are available. The full Device + Media release is still in development.") }
    }
}

@Composable fun SettingsScreen() {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SectionTitle("Privacy & roadmap", "Access is scoped to what you choose.") }
        item { FactRow("Cloud AI", "Off · not integrated", "OpenAI, Gemini, Perplexity and local inference are planned. No API key is requested in this build.") }
        item { FactRow("Media access", "Selected documents only", "Use Media → Forget selected media index to remove saved metadata and release read grants.") }
        item { FactRow("Storage", "On this device", "App-private metadata database. No additional database encryption. Backup and transfer are excluded; uninstalling removes the index.") }
        item { FactRow("Other access", "Not requested", "No notification listener, Gmail, contacts, location, broad storage, usage access or accessibility service.") }
        item { FactRow("Installed version", BuildConfig.VERSION_NAME, "No update service is connected. Available version and release date are unknown.") }
        item { FactRow("1.0 · In development", "Device + Media", "Indexing → OCR / QR / URLs → knowledge extraction → reviewed cleanup.") }
        item { FactRow("1.1 · Planned", "Gmail + notifications") }
        item { FactRow("1.2–1.4 · Planned", "Style, shopping, research", "1.2 Style Lab · 1.3 Shopping intelligence · 1.4 Perplexity research") }
        item { FactRow("2.0–3.0 · Planned", "Knowledge and local AI", "2.0 Knowledge graph · 2.1 Android capabilities · 3.0 Local AI improvements") }
    }
}
