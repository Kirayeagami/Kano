package app.kano.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import app.kano.R
import app.kano.BuildConfig
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(openDevice: () -> Unit, openMedia: () -> Unit, openStyle: () -> Unit,
    openPersonalCare: () -> Unit, openVault: () -> Unit = {}, openCamera: () -> Unit = {},
    model: KanoViewModel? = null, openShopping: () -> Unit = {}, openSavedDocuments: () -> Unit = openMedia, footerInset: Dp = 0.dp) {
    val context = LocalContext.current
    val device = model?.device?.collectAsStateWithLifecycle()?.value
    val knowledge = model?.knowledgeEntities?.collectAsStateWithLifecycle()?.value.orEmpty()
    val knowledgeError = model?.knowledgeError?.collectAsStateWithLifecycle()?.value ?: false
    val care = model?.careItems?.collectAsStateWithLifecycle()?.value
    val rows = model?.rows?.collectAsStateWithLifecycle()?.value.orEmpty()
    val scanning = model?.visionAnalyzing?.collectAsStateWithLifecycle()?.value ?: false
    var query by rememberSaveable { mutableStateOf("") }
    val clock by produceState(System.currentTimeMillis()) { while (true) { value = System.currentTimeMillis(); delay(60_000) } }
    val lower = query.lowercase()
    val focus = when {
        lower.contains("storage") || lower.contains("space") -> "Storage"
        Regex("\\bram\\b").containsMatchIn(lower) || lower.contains("memory usage") -> "RAM"
        lower.contains("battery") -> "Battery"
        lower.contains("connect") -> "Connectivity"
        lower.contains("clothes") || lower.contains("wardrobe") || lower.contains("outfit") -> "Style"
        lower.contains("cheaper") || lower.contains("shopping") -> "Shopping"
        lower.contains("skincare") || lower.contains("products") || lower.contains("care") -> "Care"
        else -> null
    }
    val tokens = lower.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    fun matches(text: String) = tokens.isNotEmpty() && tokens.all { text.lowercase().contains(it) }
    val vaultMatches = knowledge.filter { matches(it.title + " " + it.detail + " " + it.entityType) }.take(20)
    val careMatches = (care as? CareState.Ready)?.items.orEmpty().filter { matches(it.name + " " + it.category) }.take(20)
    val mediaMatches = rows.filter { matches(it.name) }.take(20)
    val data = (device as? DeviceState.Ready)?.snapshot
    val colors = KanoThemeColors
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        border = BorderStroke(0.75.dp, Color.White.copy(alpha = if (colors.isDark) 0.20f else 0.45f)),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kano_logo),
                            contentDescription = "Kano",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Text("KANO", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                }
                Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(clock)), style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
            }
            Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(clock)), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        }
        if (scanning) item { KanoStateSurface("Reading your image", "Local text and QR recognition is running.", true) }
        item {
            OutlinedTextField(query, { query = it.take(160) }, modifier = Modifier.fillMaxWidth(), label = { Text("Ask or search on this device") }, singleLine = true, shape = MaterialTheme.shapes.large)
            Text("Local commands & keyword search · cloud AI unavailable", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, modifier = Modifier.padding(top = 4.dp))
        }
        if (query.isNotBlank()) item {
            Column(Modifier.kanoMorph(LocalMotionEnabled.current), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                focus?.let { intent ->
                    KanoGlassCard {
                        Text(intent, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                        Text(when (intent) {
                            "Storage" -> data?.let { Formatter.formatFileSize(context, it.storageAvailable) + " available on the data volume" } ?: "Readings unavailable"
                            "RAM" -> data?.memoryAvailable?.let { Formatter.formatFileSize(context, it) + " system RAM available" } ?: "Readings unavailable"
                            "Battery" -> data?.batteryPercent?.let { "$it%" } ?: "Readings unavailable"
                            "Connectivity" -> data?.connectionType ?: "Readings unavailable"
                            "Style" -> "Wardrobe recognition is not configured."
                            "Care" -> (care as? CareState.Ready)?.let { "${it.items.size} user-entered records" } ?: "Inventory unavailable"
                            "Shopping" -> "No price provider is connected."
                            else -> "Readings unavailable"
                        }, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        KanoOutlinedButton(when (intent) { "Style" -> openStyle; "Care" -> openPersonalCare; "Shopping" -> openShopping; else -> openDevice }) { Text(if (intent in setOf("Style", "Care", "Shopping")) "Open $intent" else "Open Device") }
                    }
                }
                vaultMatches.forEach { record -> KanoActionRow(record.title, "Vault · " + record.entityType, openVault) }
                careMatches.forEach { record -> KanoActionRow(record.name, "Care · " + record.category, openPersonalCare) }
                mediaMatches.forEach { record -> KanoActionRow(record.name, "Saved document · current index page", openSavedDocuments) }
                if (focus == null && vaultMatches.isEmpty() && careMatches.isEmpty() && mediaMatches.isEmpty())
                    KanoStateSurface(if (knowledgeError) "Search source unavailable" else "No local match", "Searches saved Vault and Care records plus the loaded document page. Gallery filenames are searched in Media.")
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KanoOutlinedButton(openCamera) { Text("Open Kano Vision") }
                KanoOutlinedButton(openMedia) { Text("Open gallery") }
                KanoOutlinedButton(openVault) { Text("Open Knowledge Vault") }
            }
        }
        item {
            Text("Right now", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(6.dp))
            KanoCardGroup {
                KanoGroupItem("View device readings", data?.let { "${it.batteryPercent?.let { percent -> "$percent% battery" } ?: "Battery unavailable"} · " + Formatter.formatFileSize(context, it.storageAvailable) + " available" } ?: "Loading Android readings", openDevice, showDivider = true)
                KanoGroupItem("Manage Personal Care", (care as? CareState.Ready)?.let { "${it.items.size} records you entered" } ?: "Inventory loading or unavailable", openPersonalCare, showDivider = true)
                KanoGroupItem("Open Style Studio", "Your wardrobe, with explicit evidence", openStyle, showDivider = false)
            }
            Text("No calendar, notifications or email sources are connected. Daily priorities are unavailable.", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(model: KanoViewModel? = null, initialPage: String = "Overview", footerInset: Dp = 0.dp) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val colors = KanoThemeColors
    var page by rememberSaveable { mutableStateOf(initialPage) }
    LaunchedEffect(initialPage) { if (initialPage != "Overview") page = initialPage }
    val settingsList = remember(page) { LazyListState() }
    var revision by remember { mutableIntStateOf(0) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val mode = model?.themeMode?.collectAsStateWithLifecycle()?.value ?: KanoThemeMode.SYSTEM
    val visual = model?.visualTheme?.collectAsStateWithLifecycle()?.value ?: KanoVisualTheme.KANO_GLASS
    val glass = model?.glassMode?.collectAsStateWithLifecycle()?.value ?: KanoGlassMode.ON
    val reduced = model?.reducedMotion?.collectAsStateWithLifecycle()?.value ?: false
    val dark = colors.isDark
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        owner.lifecycle.addObserver(observer); onDispose { owner.lifecycle.removeObserver(observer) }
    }
    BackHandler(page != "Overview") { page = "Overview" }
    fun granted(permission: String): Boolean { revision; return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED }
    fun appSettings() {
        try { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }
        catch (_: Exception) { actionError = "Android app settings unavailable." }
    }
    LazyColumn(state = settingsList, contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionTitle(if (page == "Overview") "Privacy & Settings" else page, if (page == "Overview") "Your appearance. Your access. Your choice." else "")
            if (page != "Overview") TextButton({ page = "Overview" }) { Text("All settings") }
        }
        if (page == "Overview") {
            item {
                KanoCardGroup {
                    val settingsItems = listOf(
                        "Appearance" to "${visual.label} · ${mode.name.lowercase()}",
                        "Privacy" to "Data, processing and retention",
                        "Permissions" to "Camera and Android-controlled media",
                        "AI providers" to "Local Vision available · cloud unavailable",
                        "About" to "Version, source and capability scope"
                    )
                    settingsItems.forEachIndexed { index, (title, detail) ->
                        KanoGroupItem(title, detail, { page = title }, showDivider = index < settingsItems.lastIndex)
                    }
                }
            }
        } else when (page) {
            "Appearance" -> {
                item {
                    Text("Light & dark", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                        KanoThemeMode.entries.forEach { option ->
                            KanoOutlinedButton({ model?.setThemeMode(option) }) { Text((if (option == mode) "Selected · " else "") + option.name.lowercase().replaceFirstChar(Char::uppercaseChar)) }
                        }
                    }
                }
                item { Text("Visual themes", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary) }
                KanoVisualTheme.entries.chunked(2).forEach { pair ->
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { choice ->
                                val choiceColors = visualColors(choice, dark)
                                val choiceSemantic = visualSemanticColors(choice, dark)
                                val design = visualDesign(choice, dark)
                                Surface(onClick = { model?.setVisualTheme(choice) }, modifier = Modifier.weight(1f).semantics { selected = choice == visual },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(design.radius), color = choiceColors.background,
                                    border = androidx.compose.foundation.BorderStroke(if (choice == visual) 2.dp else 1.dp, if (choice == visual) choiceColors.primary else choiceColors.outlineVariant)) {
                                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Canvas(Modifier.fillMaxWidth().height(48.dp)) {
                                            drawRoundRect(choiceColors.secondaryContainer, size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f))
                                            drawCircle(choiceColors.primary, radius = size.height * .22f, center = androidx.compose.ui.geometry.Offset(size.width * .3f, size.height * .5f))
                                            drawRoundRect(choiceColors.surface, topLeft = androidx.compose.ui.geometry.Offset(size.width * .55f, size.height * .2f), size = androidx.compose.ui.geometry.Size(size.width * .3f, size.height * .6f))
                                        }
                                        Text((if (choice == visual) "Selected · " else "") + choice.label, color = choiceSemantic.textPrimary, style = MaterialTheme.typography.labelLarge.copy(fontFamily = design.font))
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    KanoGlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Glass OFF", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Switch(glass == KanoGlassMode.OFF, { model?.setGlassMode(if (it) KanoGlassMode.OFF else KanoGlassMode.ON) }, Modifier.semantics { contentDescription = "Glass OFF" })
                        }
                        Text("Use opaque surfaces instead of translucency. Layout, actions and motion stay the same. Ambient blur is limited to supported devices; text and photos remain sharp.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reduce motion", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Switch(reduced, { model?.setReducedMotion(it) }, Modifier.semantics { contentDescription = "Reduce motion" })
                        }
                        Text("Typography follows Android's font size. Animations stop when the app is hidden, memory is low or Android animations are disabled.", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
            "Privacy" -> item {
                KanoGlassCard {
                    FactRow("Processing", "On this device", "Local text/QR recognition. No Internet permission, telemetry or uploads.")
                    FactRow("Stored knowledge", "Explicitly reviewed items", "Recognition can be wrong. Sensitive-pattern matches block persistence; this is not a guarantee that all sensitive content is detected.")
                    FactRow("Storage", "App-private Room database", "Database-level encryption is not implemented. Backups and transfer are disabled.")
                    FactRow("Credentials", "Keystore adapter", "No live provider credentials or accounts are configured.")
                    FactRow("Revocation", "Android app settings", "Media is rechecked on resume. Forgetting the document index releases saved read grants; originals are retained.")
                    KanoOutlinedButton(::appSettings) { Text("Android app settings") }
                }
            }
            "Permissions" -> item {
                KanoGlassCard {
                    FactRow("Camera", if (granted(Manifest.permission.CAMERA)) "Granted" else "Not granted", "Requested only from Kano Vision for preview/capture. Captures are reviewed before use.")
                    FactRow("Media", model?.currentGalleryAccess()?.label ?: "Not inspected", "Full, photos-only, videos-only or selected access. Android controls which assets Kano can read.")
                    FactRow("Connectivity", "Local metadata only", "ACCESS_NETWORK_STATE reads connection type; INTERNET is removed.")
                    FactRow("Notifications / Gmail / location / contacts", "Not connected", "No listener, OAuth account, location or contact access is requested.")
                    KanoOutlinedButton(::appSettings) { Text("Manage Android permissions") }
                }
            }
            "AI providers" -> {
                item { KanoStateSurface("Local Vision", "Bundled ML Kit Latin text and QR recognition. Review each result before saving. Garment, product and movie identity are unavailable.") }
                listOf("OpenAI", "Gemini", "Perplexity", "Local language model").forEach { provider ->
                    item { KanoStateSurface("$provider · Unavailable", "No verified adapter, model or account is connected. API access is not inferred from a subscription.") }
                }
            }
            "About" -> item {
                KanoGlassCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Transparent,
                            shadowElevation = 4.dp,
                            border = BorderStroke(0.75.dp, Color.White.copy(alpha = if (colors.isDark) 0.22f else 0.50f)),
                            modifier = Modifier.size(96.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.kano_logo),
                                contentDescription = "Kano",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                            )
                        }
                    }
                    FactRow("Kano", BuildConfig.VERSION_NAME, "Development build · Device, Gallery, local Vision, Vault and manual Care inventory")
                    FactRow("Source", "Kirayeagami/Kano", "Git-backed native Kotlin and Compose application")
                    FactRow("Updates", "No update service", "Release dates and available versions are unknown.")
                    FactRow("Not configured", "Style models, Shopping, Gmail, notifications", "No fabricated recommendations, prices or account data.")
                }
            }
        }
        actionError?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
    }
}

@Composable
fun ShoppingScreen(model: KanoViewModel, openCare: () -> Unit, openCamera: () -> Unit, footerInset: Dp = 0.dp) {
    val state by model.careItems.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Shopping", "Start with what you already own.") }
        item { KanoStateSurface("Price research · Unavailable", "No seller or shopping provider is connected. Live prices, ratings, stock and price history cannot be shown.") }
        item {
            KanoCardGroup {
                KanoGroupItem("Review your inventory", (state as? CareState.Ready)?.let { "${it.items.size} products you entered" } ?: "Inventory unavailable", openCare, showDivider = true)
                KanoGroupItem("Read a product label", "Local OCR & QR · identity requires review", openCamera, showDivider = false)
            }
        }
    }
}
@Composable
fun DailyLifeScreen(footerInset: Dp = 0.dp) {
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Daily Life", "What matters today, with evidence.") }
        item { KanoStateSurface("No daily sources connected", "Calendar, tasks, email and notification sources are not connected. Kano cannot create an evidence-backed daily brief yet.") }
    }
}
