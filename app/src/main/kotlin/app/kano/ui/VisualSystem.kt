package app.kano.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*

enum class KanoVisualTheme(val label: String) {
    KANO_GLASS("Kano Glass"), SOFT_EDITORIAL("Soft Editorial"), MINIMAL_MONO("Minimal Mono"),
    WARM_STUDIO("Warm Studio"), AURORA("Aurora"), PAPER("Paper"), MIDNIGHT("Midnight"), TECHNICAL("Technical"),
}
enum class KanoGlassMode { OFF, AUTO, ON }

@Immutable
data class KanoSemanticColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val iconPrimary: Color,
    val iconSecondary: Color,
    val accent: Color,
    val accentOnSurface: Color,
    val divider: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val isDark: Boolean,
)

val LocalKanoColors = staticCompositionLocalOf {
    visualSemanticColors(KanoVisualTheme.KANO_GLASS, true)
}

val KanoThemeColors: KanoSemanticColors
    @Composable
    get() = LocalKanoColors.current

@Immutable
data class KanoDesign(val radius: Dp = 28.dp, val elevation: Dp = 2.dp,
    val ambient: Color = Color(0xFFFFDCCF), val secondaryAmbient: Color = Color(0xFFDDD9F6),
    val font: FontFamily = FontFamily.SansSerif, val glassAlpha: Float = .72f)
val LocalKanoDesign = staticCompositionLocalOf { KanoDesign() }
val LocalBlurEnabled = staticCompositionLocalOf { false }

fun visualDesign(theme: KanoVisualTheme, dark: Boolean): KanoDesign {
    val radius = when (theme) {
        KanoVisualTheme.TECHNICAL -> 14.dp; KanoVisualTheme.PAPER -> 8.dp
        KanoVisualTheme.MINIMAL_MONO -> 20.dp; KanoVisualTheme.SOFT_EDITORIAL -> 34.dp
        else -> 28.dp
    }
    val font = when (theme) { KanoVisualTheme.PAPER, KanoVisualTheme.SOFT_EDITORIAL -> FontFamily.Serif; KanoVisualTheme.TECHNICAL -> FontFamily.Monospace; else -> FontFamily.SansSerif }
    val colors = visualSemanticColors(theme, dark)
    return KanoDesign(radius, if (theme in setOf(KanoVisualTheme.PAPER, KanoVisualTheme.TECHNICAL)) 0.dp else 2.dp,
        colors.accent.copy(alpha = if (dark) .22f else .16f), colors.surfaceElevated, font,
        if (theme in setOf(KanoVisualTheme.MINIMAL_MONO, KanoVisualTheme.PAPER)) .92f else .72f)
}

fun visualSemanticColors(theme: KanoVisualTheme, dark: Boolean): KanoSemanticColors {
    return if (dark) {
        when (theme) {
            KanoVisualTheme.KANO_GLASS -> KanoSemanticColors(
                background = Color(0xFF131310), surface = Color(0xFF1E1E1A), surfaceElevated = Color(0xFF282723),
                glassSurface = Color(0xFF22211D), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFF6F4EE), textSecondary = Color(0xFFC7C5BE), textTertiary = Color(0xFF98968F), textDisabled = Color(0xFF5E5C56),
                iconPrimary = Color(0xFFF6F4EE), iconSecondary = Color(0xFFC7C5BE),
                accent = Color(0xFFFFAD91), accentOnSurface = Color(0xFFFFAD91),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
            KanoVisualTheme.SOFT_EDITORIAL -> KanoSemanticColors(
                background = Color(0xFF181317), surface = Color(0xFF241D23), surfaceElevated = Color(0xFF2E262D),
                glassSurface = Color(0xFF261F25), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFF7F1F5), textSecondary = Color(0xFFC8BFC6), textTertiary = Color(0xFF9A9198), textDisabled = Color(0xFF60585E),
                iconPrimary = Color(0xFFF7F1F5), iconSecondary = Color(0xFFC8BFC6),
                accent = Color(0xFFEAB2C9), accentOnSurface = Color(0xFFEAB2C9),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
            KanoVisualTheme.MINIMAL_MONO -> KanoSemanticColors(
                background = Color(0xFF111313), surface = Color(0xFF1D2020), surfaceElevated = Color(0xFF272A2A),
                glassSurface = Color(0xFF202323), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFF4F5F4), textSecondary = Color(0xFFC4C7C5), textTertiary = Color(0xFF949896), textDisabled = Color(0xFF5C605E),
                iconPrimary = Color(0xFFF4F5F4), iconSecondary = Color(0xFFC4C7C5),
                accent = Color(0xFFE1E5E1), accentOnSurface = Color(0xFFE1E5E1),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
            KanoVisualTheme.WARM_STUDIO -> KanoSemanticColors(
                background = Color(0xFF1A1510), surface = Color(0xFF251F19), surfaceElevated = Color(0xFF302821),
                glassSurface = Color(0xFF28221B), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFF7F2EB), textSecondary = Color(0xFFC7BFB6), textTertiary = Color(0xFF9A9188), textDisabled = Color(0xFF605850),
                iconPrimary = Color(0xFFF7F2EB), iconSecondary = Color(0xFFC7BFB6),
                accent = Color(0xFFE7BA8D), accentOnSurface = Color(0xFFE7BA8D),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
            KanoVisualTheme.AURORA -> KanoSemanticColors(
                background = Color(0xFF0F1A19), surface = Color(0xFF1A2826), surfaceElevated = Color(0xFF233533),
                glassSurface = Color(0xFF1C2A28), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFEFF7F5), textSecondary = Color(0xFFBCCBC8), textTertiary = Color(0xFF8C9D9A), textDisabled = Color(0xFF586764),
                iconPrimary = Color(0xFFEFF7F5), iconSecondary = Color(0xFFBCCBC8),
                accent = Color(0xFF8ED5CA), accentOnSurface = Color(0xFF8ED5CA),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
            KanoVisualTheme.PAPER -> KanoSemanticColors(
                background = Color(0xFF161813), surface = Color(0xFF22251D), surfaceElevated = Color(0xFF2D3127),
                glassSurface = Color(0xFF24271F), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFF5F6F1), textSecondary = Color(0xFFC3C7BE), textTertiary = Color(0xFF94998E), textDisabled = Color(0xFF5D6257),
                iconPrimary = Color(0xFFF5F6F1), iconSecondary = Color(0xFFC3C7BE),
                accent = Color(0xFFC3CDAE), accentOnSurface = Color(0xFFC3CDAE),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
            KanoVisualTheme.MIDNIGHT -> KanoSemanticColors(
                background = Color(0xFF101420), surface = Color(0xFF1B2132), surfaceElevated = Color(0xFF252D42),
                glassSurface = Color(0xFF1C2332), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFF2F4FC), textSecondary = Color(0xFFC0C7DC), textTertiary = Color(0xFF9098B0), textDisabled = Color(0xFF5A6278),
                iconPrimary = Color(0xFFF2F4FC), iconSecondary = Color(0xFFC0C7DC),
                accent = Color(0xFFBAC6FF), accentOnSurface = Color(0xFFBAC6FF),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
            KanoVisualTheme.TECHNICAL -> KanoSemanticColors(
                background = Color(0xFF101A20), surface = Color(0xFF1A2730), surfaceElevated = Color(0xFF233440),
                glassSurface = Color(0xFF1C2932), glassBorder = Color.White.copy(alpha = .15f),
                textPrimary = Color(0xFFF0F5F8), textSecondary = Color(0xFFBCC8D1), textTertiary = Color(0xFF8B9BA5), textDisabled = Color(0xFF576670),
                iconPrimary = Color(0xFFF0F5F8), iconSecondary = Color(0xFFBCC8D1),
                accent = Color(0xFFA2D0F0), accentOnSurface = Color(0xFFA2D0F0),
                divider = Color.White.copy(alpha = .08f),
                success = Color(0xFF81C784), warning = Color(0xFFFFB74D), error = Color(0xFFFF8A80),
                isDark = true
            )
        }
    } else {
        when (theme) {
            KanoVisualTheme.KANO_GLASS -> KanoSemanticColors(
                background = Color(0xFFF8F6F2), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF1B1D1B), textSecondary = Color(0xFF4C524F), textTertiary = Color(0xFF707673), textDisabled = Color(0xFFA2A7A4),
                iconPrimary = Color(0xFF1B1D1B), iconSecondary = Color(0xFF4C524F),
                accent = Color(0xFFD13C24), accentOnSurface = Color(0xFFD13C24),
                divider = Color(0xFFE4E7E4),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
            KanoVisualTheme.SOFT_EDITORIAL -> KanoSemanticColors(
                background = Color(0xFFF8F2F4), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF201B1E), textSecondary = Color(0xFF534C51), textTertiary = Color(0xFF787076), textDisabled = Color(0xFFA59EA3),
                iconPrimary = Color(0xFF201B1E), iconSecondary = Color(0xFF534C51),
                accent = Color(0xFF934F6A), accentOnSurface = Color(0xFF934F6A),
                divider = Color(0xFFE7E0E5),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
            KanoVisualTheme.MINIMAL_MONO -> KanoSemanticColors(
                background = Color(0xFFF4F4F2), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF1A1A1A), textSecondary = Color(0xFF4C4C4C), textTertiary = Color(0xFF737373), textDisabled = Color(0xFFA3A3A3),
                iconPrimary = Color(0xFF1A1A1A), iconSecondary = Color(0xFF4C4C4C),
                accent = Color(0xFF252727), accentOnSurface = Color(0xFF252727),
                divider = Color(0xFFE2E2E0),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
            KanoVisualTheme.WARM_STUDIO -> KanoSemanticColors(
                background = Color(0xFFF8F1E6), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF221C16), textSecondary = Color(0xFF544D45), textTertiary = Color(0xFF7A726A), textDisabled = Color(0xFFA69E96),
                iconPrimary = Color(0xFF221C16), iconSecondary = Color(0xFF544D45),
                accent = Color(0xFF965320), accentOnSurface = Color(0xFF965320),
                divider = Color(0xFFE7DEC9),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
            KanoVisualTheme.AURORA -> KanoSemanticColors(
                background = Color(0xFFF0F6F5), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF14201E), textSecondary = Color(0xFF455351), textTertiary = Color(0xFF6B7B79), textDisabled = Color(0xFF99A9A7),
                iconPrimary = Color(0xFF14201E), iconSecondary = Color(0xFF455351),
                accent = Color(0xFF186C67), accentOnSurface = Color(0xFF186C67),
                divider = Color(0xFFDCE6E4),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
            KanoVisualTheme.PAPER -> KanoSemanticColors(
                background = Color(0xFFF5F1E8), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF1F221C), textSecondary = Color(0xFF4F534B), textTertiary = Color(0xFF757A70), textDisabled = Color(0xFFA2A79D),
                iconPrimary = Color(0xFF1F221C), iconSecondary = Color(0xFF4F534B),
                accent = Color(0xFF4F6249), accentOnSurface = Color(0xFF4F6249),
                divider = Color(0xFFE1DCD3),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
            KanoVisualTheme.MIDNIGHT -> KanoSemanticColors(
                background = Color(0xFFF1F3FA), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF1A1E2B), textSecondary = Color(0xFF4B5162), textTertiary = Color(0xFF71788C), textDisabled = Color(0xFF9EA5B9),
                iconPrimary = Color(0xFF1A1E2B), iconSecondary = Color(0xFF4B5162),
                accent = Color(0xFF4B5A9A), accentOnSurface = Color(0xFF4B5A9A),
                divider = Color(0xFFDEE2EF),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
            KanoVisualTheme.TECHNICAL -> KanoSemanticColors(
                background = Color(0xFFF1F5F7), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFFFFFFF),
                glassSurface = Color(0xFFFFFFFF), glassBorder = Color.White.copy(alpha = .55f),
                textPrimary = Color(0xFF162026), textSecondary = Color(0xFF47535B), textTertiary = Color(0xFF6E7C85), textDisabled = Color(0xFF9BA9B2),
                iconPrimary = Color(0xFF162026), iconSecondary = Color(0xFF47535B),
                accent = Color(0xFF245C81), accentOnSurface = Color(0xFF245C81),
                divider = Color(0xFFDCE4E8),
                success = Color(0xFF2E7D32), warning = Color(0xFFE65100), error = Color(0xFFBA1A1A),
                isDark = false
            )
        }
    }
}

/** Independent light/dark canvases, surfaces, accents and ambient fields. */
fun visualColors(theme: KanoVisualTheme, dark: Boolean): ColorScheme {
    val semantic = visualSemanticColors(theme, dark)
    return (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = semantic.accent,
        onPrimary = if (dark) semantic.background else Color.White,
        background = semantic.background,
        onBackground = semantic.textPrimary,
        surface = semantic.surface,
        onSurface = semantic.textPrimary,
        surfaceVariant = semantic.surfaceElevated,
        onSurfaceVariant = semantic.textSecondary,
        primaryContainer = if (dark) semantic.surfaceElevated else semantic.accent.copy(alpha = .12f),
        onPrimaryContainer = semantic.accent,
        secondary = semantic.accent,
        onSecondary = if (dark) semantic.background else Color.White,
        secondaryContainer = semantic.surfaceElevated,
        onSecondaryContainer = semantic.textPrimary,
        tertiary = semantic.accent,
        onTertiary = if (dark) semantic.background else Color.White,
        tertiaryContainer = semantic.surfaceElevated,
        onTertiaryContainer = semantic.textPrimary,
        surfaceContainerLowest = semantic.background,
        surfaceContainerLow = semantic.surface,
        surfaceContainer = semantic.surfaceElevated,
        surfaceContainerHigh = semantic.surfaceElevated,
        surfaceContainerHighest = semantic.surfaceElevated,
        outline = semantic.textTertiary,
        outlineVariant = semantic.divider,
        error = semantic.error,
        errorContainer = if (dark) Color(0xFF563028) else Color(0xFFFBE0DB),
        onErrorContainer = if (dark) Color(0xFFFFDAD4) else Color(0xFF5B1710),
    )
}

