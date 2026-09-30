package app.kano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StyleScreen(openCamera: () -> Unit = {}, openGallery: () -> Unit = {}, footerInset: androidx.compose.ui.unit.Dp = 0.dp) {
    var page by rememberSaveable { mutableStateOf("Studio") }
    val colors = KanoThemeColors
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + footerInset), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Style Studio", "A wardrobe built from your evidence.") }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Studio", "Dress Me", "Wardrobe").forEach {
                FilterChip(page == it, { page = it }, label = { Text(it) })
            } }
        }
        item {
            KanoGlassCard {
                Text(when (page) { "Wardrobe" -> "Your wardrobe"; "Dress Me" -> "Dress Me"; else -> "Make room for your style." }, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Text("Clothing recognition and outfit previews are not configured. Kano has no verified wardrobe evidence yet.", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
        item { KanoStateSurface("Dress Me · Not configured", "No outfits, scores or recommendations are generated without verified clothing and a working model.") }
        item {
            KanoCardGroup {
                KanoGroupItem("Open Kano Vision", "Camera → review → local text & QR", openCamera, showDivider = true)
                KanoGroupItem("Choose from your gallery", "Browse images you allow Kano to read", openGallery, showDivider = false)
            }
        }
    }
}
