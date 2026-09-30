package app.kano.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.BorderStroke

@Composable
fun KanoCardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = KanoThemeColors
    val glass = LocalGlassEnabled.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (glass) colors.surface.copy(alpha = LocalKanoDesign.current.glassAlpha) else colors.surfaceElevated,
        border = BorderStroke(0.75.dp, colors.glassBorder),
        shadowElevation = if (glass) LocalKanoDesign.current.elevation else 2.dp,
        content = { Column(Modifier.fillMaxWidth(), content = content) }
    )
}

@Composable
fun KanoGroupItem(
    title: String,
    detail: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    showDivider: Boolean = true
) {
    val colors = KanoThemeColors
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .kanoPressScale(interaction, pressedScale = KanoMotionTokens.ScaleCardPressed)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            icon?.let { Icon(it, null, tint = colors.accent, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = colors.textTertiary, modifier = Modifier.size(16.dp))
        }
    }
    if (showDivider) HorizontalDivider(color = colors.divider, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun KanoActionRow(title: String, detail: String, onClick: () -> Unit, icon: ImageVector? = null) {
    val colors = KanoThemeColors
    val glass = LocalGlassEnabled.current
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (glass) colors.surface.copy(alpha = LocalKanoDesign.current.glassAlpha) else colors.surfaceElevated,
        border = BorderStroke(0.75.dp, colors.glassBorder),
        shadowElevation = if (glass) LocalKanoDesign.current.elevation else 2.dp,
        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            icon?.let { Icon(it, null, tint = colors.accent, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun KanoStateSurface(title: String, detail: String, processing: Boolean = false) {
    val colors = KanoThemeColors
    KanoGlassCard {
        if (processing) KanoSignal(true, Modifier.fillMaxWidth().height(52.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
    }
}
/** Ambient geometry; processing=true only for an actual running operation. */
@Composable
fun KanoSignal(processing: Boolean, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondaryContainer
    val phase = if (LocalMotionEnabled.current && processing) {
        val motion = rememberInfiniteTransition(label = "local operation")
        val value by motion.animateFloat(.35f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "processing")
        value
    } else .6f
    Canvas(modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        drawCircle(Brush.radialGradient(listOf(secondary.copy(alpha = .9f), Color.Transparent), center, size.height * .6f), size.height * .6f, center)
        drawCircle(Brush.radialGradient(listOf(primary.copy(alpha = .7f), primary.copy(alpha = .06f)), center, size.height * .32f), size.height * (.22f + phase * .1f), center)
    }
}
fun Modifier.kanoMorph(motion: Boolean) = if (motion) animateContentSize(tween(KanoMotion.CONTENT)) else this

@Composable
fun MetricRow(
    label: String,
    value: String,
    source: String,
    freshness: String = "Live",
    status: String = "Available",
    note: String? = null,
    showDivider: Boolean = true
) {
    val colors = KanoThemeColors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            StatusChip(
                status,
                tone = when {
                    status.contains("Available", ignoreCase = true) || status.contains("Good", ignoreCase = true) || status.contains("Normal", ignoreCase = true) -> StatusTone.SUCCESS
                    status.contains("Unavailable", ignoreCase = true) || status.contains("Absent", ignoreCase = true) -> StatusTone.NEUTRAL
                    status.contains("Throttling", ignoreCase = true) || status.contains("Low", ignoreCase = true) || status.contains("Warning", ignoreCase = true) -> StatusTone.WARNING
                    status.contains("Permission", ignoreCase = true) || status.contains("Restricted", ignoreCase = true) -> StatusTone.ACCENT
                    else -> StatusTone.NEUTRAL
                }
            )
        }
        Text(value, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Source: $source", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
            Text(freshness, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
        }
        note?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
    if (showDivider) HorizontalDivider(color = colors.divider)
}

