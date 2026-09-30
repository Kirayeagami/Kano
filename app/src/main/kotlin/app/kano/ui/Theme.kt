package app.kano.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val KanoPeachContainer: Color @Composable get() = LocalKanoDesign.current.ambient
val KanoLavenderContainer: Color @Composable get() = LocalKanoDesign.current.secondaryAmbient
val KanoGreenContainer: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
val KanoBlueContainer: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val KanoCoralPrimary: Color @Composable get() = MaterialTheme.colorScheme.primary

@Composable
fun KanoTheme(themeMode: KanoThemeMode = KanoThemeMode.SYSTEM,
    visualTheme: KanoVisualTheme = KanoVisualTheme.KANO_GLASS, content: @Composable () -> Unit) {
    val dark = when (themeMode) { KanoThemeMode.LIGHT -> false; KanoThemeMode.DARK -> true; KanoThemeMode.SYSTEM -> isSystemInDarkTheme() }
    val design = visualDesign(visualTheme, dark)
    val semanticColors = visualSemanticColors(visualTheme, dark)
    val base = TextStyle(fontFamily = design.font, color = Color.Unspecified)
    CompositionLocalProvider(
        LocalKanoDesign provides design,
        LocalKanoColors provides semanticColors
    ) {
        MaterialTheme(colorScheme = visualColors(visualTheme, dark),
            typography = Typography(
                displaySmall = base.copy(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
                headlineLarge = base.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
                headlineMedium = base.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
                headlineSmall = base.copy(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium),
                titleLarge = base.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
                titleMedium = base.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
                titleSmall = base.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
                bodyLarge = base.copy(fontSize = 15.sp, lineHeight = 22.sp),
                bodyMedium = base.copy(fontSize = 14.sp, lineHeight = 20.sp),
                bodySmall = base.copy(fontSize = 12.sp, lineHeight = 16.sp),
                labelLarge = base.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
                labelMedium = base.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
                labelSmall = base.copy(fontSize = 11.sp, lineHeight = 14.sp),
            ),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(6.dp),
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(design.radius * .65f),
                large = RoundedCornerShape(design.radius),
                extraLarge = RoundedCornerShape(design.radius + 6.dp)
            ),
            content = content)
    }
}
@Composable
fun KanoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .kanoPressScale(interactions, pressedScale = KanoMotionTokens.ScalePressed, enabled = enabled),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        interactionSource = interactions,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun KanoHeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    KanoButton(onClick, modifier.fillMaxWidth(), enabled, content = content)
}

@Composable
fun KanoOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactions = remember { MutableInteractionSource() }
    val colors = KanoThemeColors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .kanoPressScale(interactions, pressedScale = KanoMotionTokens.ScalePressed, enabled = enabled),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        interactionSource = interactions,
        border = BorderStroke(1.dp, if (enabled) colors.divider else colors.divider.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.accent,
            disabledContentColor = colors.textDisabled
        ),
        contentPadding = contentPadding,
        content = content
    )
}

enum class StatusTone { NEUTRAL, ACCENT, SUCCESS, WARNING, ERROR }

@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.NEUTRAL,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val colors = KanoThemeColors
    val resolvedTone = when {
        text.contains("ERROR", ignoreCase = true) -> StatusTone.ERROR
        text.contains("REVOKED", ignoreCase = true) -> StatusTone.WARNING
        text.contains("LOW", ignoreCase = true) || text.contains("NEARLY", ignoreCase = true) -> StatusTone.WARNING
        text.contains("ACTIVE", ignoreCase = true) || text.contains("Granted", ignoreCase = true) -> StatusTone.SUCCESS
        text.contains("UNCERTAIN", ignoreCase = true) -> StatusTone.ACCENT
        else -> tone
    }
    val defaultBg = when (resolvedTone) {
        StatusTone.NEUTRAL -> if (colors.isDark) Color(0xFF2B2D2B) else Color(0xFFEEF0ED)
        StatusTone.ACCENT -> if (colors.isDark) colors.accent.copy(alpha = 0.18f) else colors.accent.copy(alpha = 0.12f)
        StatusTone.SUCCESS -> if (colors.isDark) colors.success.copy(alpha = 0.18f) else colors.success.copy(alpha = 0.12f)
        StatusTone.WARNING -> if (colors.isDark) colors.warning.copy(alpha = 0.20f) else colors.warning.copy(alpha = 0.14f)
        StatusTone.ERROR -> if (colors.isDark) colors.error.copy(alpha = 0.18f) else colors.error.copy(alpha = 0.12f)
    }
    val defaultContent = when (resolvedTone) {
        StatusTone.NEUTRAL -> colors.textSecondary
        StatusTone.ACCENT -> colors.accent
        StatusTone.SUCCESS -> colors.success
        StatusTone.WARNING -> colors.warning
        StatusTone.ERROR -> colors.error
    }
    val bg = containerColor ?: defaultBg
    val content = contentColor ?: defaultContent
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(0.5.dp, content.copy(alpha = if (colors.isDark) 0.35f else 0.25f))
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
@Composable
fun SectionTitle(title: String, detail: String) {
    val colors = KanoThemeColors
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary, modifier = Modifier.semantics { heading() })
        if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
    }
}
@Composable
fun FactRow(label: String, value: String, detail: String? = null) {
    val colors = KanoThemeColors
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary) }
    }
    HorizontalDivider(color = colors.divider)
}

