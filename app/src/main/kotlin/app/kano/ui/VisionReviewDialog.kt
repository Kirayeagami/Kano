package app.kano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionReviewDialog(model: KanoViewModel) {
    val result by model.visionResult.collectAsStateWithLifecycle()
    val analyzing by model.visionAnalyzing.collectAsStateWithLifecycle()
    val saving by model.visionSaving.collectAsStateWithLifecycle()
    val error by model.visionError.collectAsStateWithLifecycle()
    if (analyzing) ModalBottomSheet(onDismissRequest = model::cancelLocalVision, containerColor = KanoThemeColors.surface) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            KanoSignal(true, Modifier.fillMaxWidth().height(88.dp))
            Text("Recognizing on this device", style = MaterialTheme.typography.headlineMedium, color = KanoThemeColors.textPrimary)
            Text("Text and QR analysis is running. No result is assumed.", style = MaterialTheme.typography.bodyMedium, color = KanoThemeColors.textSecondary)
            Spacer(Modifier.height(12.dp))
            KanoOutlinedButton(model::cancelLocalVision) { Text("Stop analysis") }
        }
    }
    result?.let { review ->
        ModalBottomSheet(onDismissRequest = { if (!saving) model.visionResult.value = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { it != SheetValue.Hidden || !saving }),
            containerColor = KanoThemeColors.surface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Kano Vision · Review", "Local recognition · check every result")
                Text("Nothing is saved until you choose an item. Recognition may contain errors.", style = MaterialTheme.typography.bodyMedium, color = KanoThemeColors.textSecondary)
                if (review.isSensitiveRedacted) KanoStateSurface("Sensitive content withheld", "Raw text and QR data are hidden. Saving is blocked.")
                review.issues.forEach { Text(it, color = KanoThemeColors.error) }
                if (review.candidates.isEmpty() && !review.isSensitiveRedacted) KanoStateSurface("No supported result", "No text or safe web address was extracted.")
                review.candidates.forEach { candidate ->
                    KanoGlassCard {
                        Text(candidate.title, style = MaterialTheme.typography.titleMedium, color = KanoThemeColors.textPrimary)
                        StatusChip("UNCERTAIN · " + candidate.extraction.name)
                        Text(candidate.url ?: candidate.detail, color = KanoThemeColors.textSecondary)
                        KanoOutlinedButton({ model.saveVisionCandidate(candidate) }, enabled = !saving) { Text(if (saving) "Verifying source…" else "Save reviewed item to Vault") }
                    }
                }
                if (!review.qrPayload.isNullOrBlank() && review.candidates.none { it.url != null })
                    Text("A non-web QR code was detected. Saving or opening its payload is unavailable.", color = KanoThemeColors.textTertiary)
                error?.let { Text(it, color = KanoThemeColors.error) }
                KanoOutlinedButton({ model.visionResult.value = null }, enabled = !saving) { Text("Done") }
            }
        }
    }
}
