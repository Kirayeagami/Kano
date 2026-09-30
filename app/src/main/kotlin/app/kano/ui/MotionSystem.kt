package app.kano.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Standardized Kano Motion & Micro-interaction Tokens.
 *
 * Micro: 100-180ms for buttons, icons, and small feedback.
 * Standard: 180-280ms for cards, sheets, and controls.
 * Major: 280-450ms for screen transitions and media expansions.
 * Ambient: 8-16s for gentle fluid drift.
 */
object KanoMotionTokens {
    // Duration tokens (ms)
    const val MICRO_FAST = 120
    const val MICRO = 160
    const val STANDARD_FAST = 200
    const val STANDARD = 240
    const val MAJOR_FAST = 320
    const val MAJOR = 380
    const val AMBIENT_SLOW = 14000
    const val AMBIENT_FAST = 8000

    // Easing curves
    val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val FluidEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

    // Spring specifications
    val SpringTactile = spring<Float>(dampingRatio = 0.85f, stiffness = 600f)
    val SpringGentle = spring<Float>(dampingRatio = 0.90f, stiffness = 380f)
    val SpringNav = spring<Float>(dampingRatio = 0.88f, stiffness = 420f)
    val SpringBounce = spring<Float>(dampingRatio = 0.78f, stiffness = 450f)

    // Tactile scale tokens
    const val ScalePressed = 0.96f
    const val ScaleSelected = 1.04f
    const val ScaleCardPressed = 0.985f

    // Spatial slide offsets
    val SlideDistanceScreen = 20.dp
    val SlideDistanceCard = 10.dp
    val SlideDistanceMicro = 4.dp
}

/**
 * Kano Brand Color Tokens derived directly from the official Kano mascot artwork:
 * Mint-green mascot, cherry blossom pinks, cream canvas, soft peach, and translucent glass.
 */
object KanoBrandTokens {
    val MintPrimary = Color(0xFF68C99B)
    val MintDark = Color(0xFF285E46)
    val MintSoft = Color(0xFFD4EEDF)
    val SakuraPink = Color(0xFFF9A8B6)
    val SakuraSoft = Color(0xFFFDE8EC)
    val CreamCanvas = Color(0xFFF7F9F5)
    val PeachAccent = Color(0xFFFFB088)
    val GlassWhite = Color(0xFFFFFFFF)
    val GlassHighlight = Color(0x66FFFFFF)

    /**
     * Resolves the ambient wave color for each screen route,
     * maintaining section personality while anchoring with Kano's brand identity.
     */
    fun sectionWaveColor(route: String?, isDark: Boolean): Color = when (route) {
        "home" -> if (isDark) Color(0xFFFF6C4B) else Color(0xFFFF8A65)
        "device", "diagnostics" -> if (isDark) Color(0xFF0284C7) else Color(0xFF38BDF8)
        "media", "saved_media" -> if (isDark) Color(0xFF6366F1) else Color(0xFF818CF8)
        "vault" -> if (isDark) Color(0xFFD97706) else Color(0xFFF59E0B)
        "style" -> if (isDark) Color(0xFFE11D48) else Color(0xFFFB7185)
        "personal_care", "care" -> if (isDark) MintDark else MintPrimary
        else -> if (isDark) Color(0xFF3B82F6) else Color(0xFF60A5FA)
    }
}

/**
 * Tactile button press feedback modifier with spring physics.
 * Scales down subtly on press and springs back on release.
 * Collapses to no-op when motion is disabled or reduced.
 */
@Composable
fun Modifier.kanoPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = KanoMotionTokens.ScalePressed,
    enabled: Boolean = true
): Modifier {
    val motion = LocalMotionEnabled.current
    if (!motion || !enabled) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = KanoMotionTokens.SpringTactile,
        label = "kanoPressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Subtle item entrance animation modifier for lists and cards.
 * Fades in and slides vertically with a gentle stagger delay.
 */
@Composable
fun Modifier.kanoItemEntrance(
    index: Int = 0,
    offsetY: Dp = KanoMotionTokens.SlideDistanceCard
): Modifier {
    val motion = LocalMotionEnabled.current
    if (!motion) return this

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val delayMs = (index * 30L).coerceAtMost(180L)
        if (delayMs > 0) delay(delayMs)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(KanoMotionTokens.STANDARD, easing = KanoMotionTokens.EmphasizedDecelerate),
        label = "kanoEntranceAlpha"
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else offsetY.value,
        animationSpec = tween(KanoMotionTokens.STANDARD, easing = KanoMotionTokens.EmphasizedDecelerate),
        label = "kanoEntranceY"
    )

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY * density
    }
}
