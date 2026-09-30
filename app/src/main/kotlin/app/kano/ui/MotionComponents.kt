package app.kano.ui

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalGlassEnabled = staticCompositionLocalOf { true }
val LocalMotionEnabled = staticCompositionLocalOf { false }

object KanoMotion {
    const val CONTENT = 240
    const val SCREEN = 320
    const val AMBIENT = 12000
}

@Composable
fun KanoGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = LocalKanoDesign.current.radius,
    glassColor: Color = KanoThemeColors.surface,
    borderColor: Color? = null,
    liquidHighlight: Boolean = false,
    content: @Composable () -> Unit
) {
    val design = LocalKanoDesign.current
    val colors = KanoThemeColors
    val glass = LocalGlassEnabled.current
    val motion = LocalMotionEnabled.current
    val dark = colors.isDark

    // Slow, subtle ambient drift for liquid glass reflection
    val highlight = if (liquidHighlight && glass && motion) {
        val transition = rememberInfiniteTransition(label = "glass reflection")
        val value by transition.animateFloat(
            initialValue = -0.25f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(KanoMotionTokens.AMBIENT_SLOW, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "reflection"
        )
        value
    } else 0.25f

    // Low-amplitude fluid breathing under glass
    val fluidShift = if (glass && motion) {
        val transition = rememberInfiniteTransition(label = "fluid under glass")
        val shift by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(KanoMotionTokens.AMBIENT_FAST, easing = KanoMotionTokens.FluidEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fluid shift"
        )
        shift
    } else 0.35f

    val resolvedBorder = borderColor ?: colors.glassBorder

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = if (glass) glassColor.copy(alpha = design.glassAlpha) else colors.surfaceElevated,
        shadowElevation = if (glass) design.elevation else 2.dp,
        border = BorderStroke(0.75.dp, resolvedBorder)
    ) {
        Box {
            // Ambient fluid wash under the translucent surface
            if (glass) {
                Canvas(
                    Modifier.matchParentSize().then(
                        if (LocalBlurEnabled.current && Build.VERSION.SDK_INT >= 31)
                            Modifier.blur(24.dp, BlurredEdgeTreatment.Unbounded)
                        else Modifier
                    )
                ) {
                    val washAlpha = if (dark) 0.14f else 0.22f
                    drawCircle(
                        Brush.radialGradient(
                            listOf(
                                design.ambient.copy(alpha = washAlpha + fluidShift * 0.04f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * (0.65f + fluidShift * 0.08f),
                        center = Offset(size.width * 0.85f, size.height * 0.15f)
                    )
                }
            }

            // Subtle specular edge highlight for polished liquid glass
            if (glass) {
                Canvas(Modifier.matchParentSize()) {
                    drawLine(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (dark) 0.18f else 0.45f),
                                Color.Transparent
                            ),
                            startX = size.width * 0.15f,
                            endX = size.width * 0.85f
                        ),
                        Offset(cornerRadius.toPx(), 1.dp.toPx()),
                        Offset(size.width - cornerRadius.toPx(), 1.dp.toPx()),
                        strokeWidth = 0.75.dp.toPx()
                    )
                }
            }

            // Optional localized liquid highlight sweep
            if (liquidHighlight && glass) {
                Canvas(Modifier.matchParentSize()) {
                    drawRect(
                        Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            start = Offset(size.width * highlight, 0f),
                            end = Offset(size.width * (highlight + 0.35f), size.height)
                        )
                    )
                }
            }

            content()
        }
    }
}

@Composable
fun KanoGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = LocalKanoDesign.current.radius,
    glassColor: Color = KanoThemeColors.surface,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    KanoGlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = cornerRadius,
        glassColor = glassColor,
        borderColor = borderColor
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun KanoMenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val glass = LocalGlassEnabled.current
    val colors = KanoThemeColors
    val dark = colors.isDark
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(20.dp)

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(42.dp)
            .kanoPressScale(interactionSource = interaction, pressedScale = KanoMotionTokens.ScalePressed)
            .semantics { contentDescription = "Open menu" },
        shape = shape,
        color = if (glass) (if (dark) Color.Black.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.65f)) else colors.surfaceElevated,
        shadowElevation = if (glass) 1.5.dp else 2.dp,
        border = BorderStroke(0.75.dp, colors.glassBorder),
        interactionSource = interaction
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Outlined.Menu,
                contentDescription = null,
                tint = colors.iconPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun KanoWaveBackground(
    modifier: Modifier = Modifier,
    waveColor: Color = LocalKanoDesign.current.ambient,
    secondaryColor: Color = LocalKanoDesign.current.secondaryAmbient
) {
    val motion = LocalMotionEnabled.current
    val dark = KanoThemeColors.isDark

    // Animate color transition when navigating between sections
    val animatedWaveColor by animateColorAsState(
        targetValue = waveColor,
        animationSpec = tween(KanoMotionTokens.MAJOR, easing = KanoMotionTokens.FluidEasing),
        label = "waveColorAnim"
    )
    val animatedSecondaryColor by animateColorAsState(
        targetValue = secondaryColor,
        animationSpec = tween(KanoMotionTokens.MAJOR, easing = KanoMotionTokens.FluidEasing),
        label = "secondaryColorAnim"
    )

    // Very gentle fluid drift
    val shift = if (motion) {
        val transition = rememberInfiniteTransition(label = "ambient fluid wave")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(KanoMotionTokens.AMBIENT_SLOW, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "waveShift"
        )
        value
    } else 0.35f

    Canvas(modifier.fillMaxSize()) {
        // Wave 1: Primary organic curve
        val path1 = Path().apply {
            moveTo(0f, size.height * 0.12f)
            cubicTo(
                size.width * 0.30f,
                size.height * (0.24f + shift * 0.05f),
                size.width * 0.65f,
                size.height * (0.04f + shift * 0.06f),
                size.width,
                size.height * 0.26f
            )
            lineTo(size.width, 0f)
            lineTo(0f, 0f)
            close()
        }
        val alpha1 = if (dark) 0.32f else 0.22f
        drawPath(
            path1,
            Brush.verticalGradient(
                listOf(animatedWaveColor.copy(alpha = alpha1), Color.Transparent),
                endY = size.height * 0.48f
            )
        )

        // Wave 2: Secondary organic fluid counter-wave
        val alpha2 = if (dark) 0.20f else 0.15f
        drawCircle(
            Brush.radialGradient(
                listOf(animatedSecondaryColor.copy(alpha = alpha2), Color.Transparent)
            ),
            radius = size.width * 0.75f,
            center = Offset(
                size.width * (0.55f + shift * 0.15f),
                size.height * (0.42f + shift * 0.06f)
            )
        )
    }
}
