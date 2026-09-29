package app.kano.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalGlassEnabled = staticCompositionLocalOf { true }
val LocalMotionEnabled = staticCompositionLocalOf { false }

// Readable translucent surfaces; no claim of a backdrop blur implementation.
@Composable
fun KanoGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    glassColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = if (LocalGlassEnabled.current) glassColor else glassColor.copy(alpha = 1f),
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, borderColor),
        content = content,
    )
}

// Translucent Glassmorphism Card with Content Padding
@Composable
fun KanoGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    glassColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    content: @Composable ColumnScope.() -> Unit,
) {
    KanoGlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = cornerRadius,
        glassColor = glassColor,
        borderColor = borderColor,
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

// Animated Card with Entrance Fade/Slide & Press Scale Feedback
@Composable
fun KanoAnimatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleState by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cardScale",
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(350, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it / 6 },
        exit = fadeOut() + slideOutVertically(),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scaleState
                    scaleY = scaleState
                }
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick,
                        )
                    } else Modifier,
                ),
            shape = RoundedCornerShape(cornerRadius),
            color = backgroundColor,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f)),
        ) {
            Column(modifier = Modifier.padding(18.dp), content = content)
        }
    }
}

// Floating Circular Control Button
@Composable
fun KanoFloatingControl(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 52.dp,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(size.coerceAtLeast(48.dp)),
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
            )
        }
    }
}

// Ambient Background Wave Drawing Layer
@Composable
fun KanoWaveBackground(
    modifier: Modifier = Modifier,
    waveColor: Color = KanoPeachContainer.copy(alpha = 0.45f),
) {
    val waveShift = if (LocalMotionEnabled.current) {
        val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
        val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "waveShift",
        )
        shift
    } else 0f

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, height * 0.2f)
            quadraticTo(
                width * 0.5f + waveShift,
                height * 0.05f,
                width,
                height * 0.25f,
            )
            lineTo(width, 0f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(path = path, color = waveColor)
    }
}

// Gradient Hero Card Surface
@Composable
fun KanoGradientHero(
    modifier: Modifier = Modifier,
    startColor: Color = KanoCoralGradientStart,
    endColor: Color = KanoCoralGradientEnd,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val brush = Brush.linearGradient(listOf(startColor, endColor))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
            .padding(20.dp),
    ) {
        Column(content = content)
    }
}

// Animated Content Numeric / String Transition
@Composable
fun KanoAnimatedNumericText(
    value: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn()) togetherWith
                (slideOutVertically { height -> -height } + fadeOut())
        },
        label = "numericText",
    ) { targetText ->
        Text(
            text = targetText,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = fontWeight,
            color = color,
            modifier = modifier,
        )
    }
}
