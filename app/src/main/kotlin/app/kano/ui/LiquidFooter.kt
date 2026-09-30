package app.kano.ui

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import app.kano.NavDestination
import kotlin.math.abs

/** Draw-only backdrop references. Neither source records the footer or captures a bitmap. */
@Stable
class FooterBackdrop(val background: GraphicsLayer, val content: GraphicsLayer) {
    var contentOrigin by mutableStateOf(Offset.Zero)
}

@Composable
fun LiquidFooter(
    destinations: List<NavDestination>,
    route: String?,
    navigate: (String) -> Unit,
    backdrop: FooterBackdrop,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassEnabled.current
    val motion = LocalMotionEnabled.current
    val blur = glass && LocalBlurEnabled.current && Build.VERSION.SDK_INT >= 31
    val colors = KanoThemeColors
    val dark = colors.isDark
    val surface = colors.surface
    val shape = RoundedCornerShape(26.dp)
    val frame = rememberGraphicsLayer()
    var origin by remember { mutableStateOf(Offset.Zero) }

    val selectedIndex = destinations.indexOfFirst { it.route == route }
    var lastIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedIndex) { if (selectedIndex >= 0) lastIndex = selectedIndex }

    val targetPos = (if (selectedIndex >= 0) selectedIndex else lastIndex).toFloat()
    val position by animateFloatAsState(
        targetValue = targetPos,
        animationSpec = if (motion) KanoMotionTokens.SpringNav else snap(),
        label = "footer selection position"
    )

    val selectionAlpha by animateFloatAsState(
        targetValue = if (selectedIndex >= 0) 1f else 0f,
        animationSpec = tween(if (motion) KanoMotionTokens.STANDARD_FAST else 0),
        label = "footer selection visibility"
    )

    var pressedIndex by remember { mutableIntStateOf(-1) }
    val pressCompression by animateFloatAsState(
        targetValue = if (pressedIndex >= 0) 0.92f else 0.88f,
        animationSpec = if (motion) KanoMotionTokens.SpringTactile else snap(),
        label = "footer press depth"
    )

    // Subtle fluid wave drift across the glass bar
    val reflection = if (glass && motion) {
        val cycle = rememberInfiniteTransition(label = "footer fluid reflection")
        val value by cycle.animateFloat(
            initialValue = 0.10f,
            targetValue = 0.90f,
            animationSpec = infiniteRepeatable(
                animation = tween(KanoMotionTokens.AMBIENT_SLOW, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "reflection shift"
        )
        value
    } else 0.35f

    Box(
        modifier
            .shadow(if (glass) 2.dp else 3.dp, shape, clip = false,
                ambientColor = Color.Black.copy(alpha = .12f), spotColor = Color.Black.copy(alpha = .12f))
            .clip(shape)
            .onGloballyPositioned { origin = it.positionInRoot() }
            .testTag("kano-footer")
            .drawWithContent {
                if (glass) {
                    // Sample backdrop at exact footer position
                    frame.record {
                        withTransform({ translate(-origin.x, -origin.y) }) { drawLayer(backdrop.background) }
                        withTransform({ translate(backdrop.contentOrigin.x - origin.x, backdrop.contentOrigin.y - origin.y) }) {
                            drawLayer(backdrop.content)
                        }
                    }
                    frame.renderEffect = if (blur) BlurEffect(10.dp.toPx(), 10.dp.toPx(), TileMode.Clamp) else null
                    drawLayer(frame)

                    // Translucent fluid glass base layer
                    drawRect(if (dark) Color.Black.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.10f))
                    drawRect(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (dark) 0.07f else 0.11f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Outer perimeter edge highlight
                    val edge = 0.5.dp.toPx()
                    drawRoundRect(
                        Color.White.copy(alpha = if (dark) 0.14f else 0.22f),
                        topLeft = Offset(edge, edge),
                        size = Size(size.width - edge * 2, size.height - edge * 2),
                        cornerRadius = CornerRadius(26.dp.toPx()),
                        style = Stroke(edge)
                    )

                    // Specular light beam that breathes gently
                    drawLine(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (dark) 0.20f else 0.32f),
                                Color.Transparent
                            ),
                            startX = size.width * (reflection - 0.20f),
                            endX = size.width * (reflection + 0.20f)
                        ),
                        Offset(26.dp.toPx(), 1.dp.toPx()),
                        Offset(size.width - 26.dp.toPx(), 1.dp.toPx()),
                        edge
                    )
                } else {
                    drawRect(surface)
                }

                // Fluid lens morph calculation
                val padding = 4.dp.toPx()
                val cell = (size.width - padding * 2) / destinations.size
                val flightDelta = abs(targetPos - position)
                val fluidStretch = if (motion) (flightDelta * 0.15f).coerceAtMost(0.20f) else 0f
                val indicatorWidth = cell * (pressCompression + fluidStretch)
                val left = padding + cell * position + (cell - indicatorWidth) / 2
                val indicatorHeight = 40.dp.toPx()
                val indicatorTop = (size.height - indicatorHeight) / 2

                // One moving neutral lens, sampled inside the glass rather than a colored tile.
                if (selectionAlpha > 0f) {
                    val lens = if (dark) Color.White else Color.Black
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                lens.copy(alpha = (if (dark) 0.09f else 0.045f) * selectionAlpha),
                                lens.copy(alpha = 0.02f * selectionAlpha)
                            )
                        ),
                        topLeft = Offset(left, indicatorTop),
                        size = Size(indicatorWidth, indicatorHeight),
                        cornerRadius = CornerRadius(20.dp.toPx())
                    )
                    // Fluid highlight border
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.12f * selectionAlpha),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(left, indicatorTop),
                        size = Size(indicatorWidth, indicatorHeight),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        style = Stroke(0.5.dp.toPx())
                    )
                }
                drawContent()
            }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { index, destination ->
                val isSelected = selectedIndex == index
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                LaunchedEffect(pressed) {
                    if (pressed) pressedIndex = index else if (pressedIndex == index) pressedIndex = -1
                }

                // Tactile scale on press
                val pressScale by animateFloatAsState(
                    targetValue = if (pressed && motion) KanoMotionTokens.ScalePressed else 1f,
                    animationSpec = if (motion) KanoMotionTokens.SpringTactile else snap(),
                    label = "footer icon press"
                )

                val iconAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.68f,
                    animationSpec = tween(if (motion) KanoMotionTokens.STANDARD_FAST else 0),
                    label = "footer icon alpha"
                )

                val tintColor = colors.textPrimary
                val totalScale = pressScale

                Surface(
                    onClick = { navigate(destination.route) },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(24.dp),
                    interactionSource = interaction,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics {
                            selected = isSelected
                            role = Role.Tab
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // High-contrast shadow silhouette for outdoor/bright photo legibility
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                            tint = (if (dark) Color.Black else Color.White).copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = totalScale
                                    scaleY = totalScale
                                }
                                .drawWithContent {
                                    val edge = 0.5.dp.toPx()
                                    withTransform({ translate(edge, 0f) }) { this@drawWithContent.drawContent() }
                                    withTransform({ translate(-edge, 0f) }) { this@drawWithContent.drawContent() }
                                    withTransform({ translate(0f, edge) }) { this@drawWithContent.drawContent() }
                                    withTransform({ translate(0f, -edge) }) { this@drawWithContent.drawContent() }
                                }
                        )

                        // Icons share one baseline; only contrast and press scale change.
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            tint = tintColor.copy(alpha = iconAlpha),
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer {
                                    scaleX = totalScale
                                    scaleY = totalScale
                                }
                        )
                    }
                }
            }
        }
    }
}
