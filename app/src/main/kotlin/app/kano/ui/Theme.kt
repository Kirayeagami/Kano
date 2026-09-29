package app.kano.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// --- Stitch Prototype "Warm Glass Intelligence" Palette ---
val KanoCoralPrimaryLight = Color(0xFFFF3B1D) // Vibrant Red-Orange Primary
val KanoCoralGradientStartLight = Color(0xFFFF5A36)
val KanoCoralGradientEndLight = Color(0xFFE82E0E)
val KanoPeachAccentLight = Color(0xFFFF9F43) // Warm Peach Secondary
val KanoOffWhiteBgLight = Color(0xFFF8F6F2) // Warm Off-White Canvas
val KanoSurfaceWhiteLight = Color(0xFFFFFFFF)
val KanoSurfaceNeutralLight = Color(0xFFF1EEE8)
val KanoSurfaceNeutralHighLight = Color(0xFFE6E2D8)
val KanoInkPrimaryLight = Color(0xFF111111) // Near-Black Text
val KanoInkSecondaryLight = Color(0xFF666666) // Muted Charcoal
val KanoPeachContainerLight = Color(0xFFFFEAE5)
val KanoBlueContainerLight = Color(0xFFE8F2FF)
val KanoGreenContainerLight = Color(0xFFE8F8F0)
val KanoLavenderContainerLight = Color(0xFFF0ECF9)

// --- Stitch Nothing-Inspired Dark Palette ---
val KanoCoralPrimaryDark = Color(0xFFFF3B1D) // Vibrant Red-Orange Accent
val KanoCoralGradientStartDark = Color(0xFFFF5232)
val KanoCoralGradientEndDark = Color(0xFFD82808)
val KanoPeachAccentDark = Color(0xFFFF9F43)
val KanoOffWhiteBgDark = Color(0xFF000000) // Pure Black Canvas
val KanoSurfaceWhiteDark = Color(0xFF161616) // Deep Charcoal Surface
val KanoSurfaceNeutralDark = Color(0xFF222222) // Elevated Surface
val KanoSurfaceNeutralHighDark = Color(0xFF2C2C2C)
val KanoInkPrimaryDark = Color(0xFFFFFFFF) // Crisp Warm White
val KanoInkSecondaryDark = Color(0xFFA0A0A0)
val KanoPeachContainerDark = Color(0xFF3B120B)
val KanoBlueContainerDark = Color(0xFF10253B)
val KanoGreenContainerDark = Color(0xFF0E2E1B)
val KanoLavenderContainerDark = Color(0xFF221A3B)

// Composition-scoped tokens
val KanoPeachContainer: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
val KanoLavenderContainer: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val KanoGreenContainer: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
val KanoBlueContainer: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val KanoCoralGradientStart: Color @Composable get() = MaterialTheme.colorScheme.primary
val KanoCoralGradientEnd: Color @Composable get() = MaterialTheme.colorScheme.primary
val KanoCoralPrimary: Color @Composable get() = MaterialTheme.colorScheme.primary

private val KanoLightColors = lightColorScheme(
    primary = KanoCoralPrimaryLight,
    onPrimary = Color.White,
    background = KanoOffWhiteBgLight,
    onBackground = KanoInkPrimaryLight,
    surface = KanoSurfaceWhiteLight,
    onSurface = KanoInkPrimaryLight,
    surfaceVariant = KanoSurfaceNeutralLight,
    onSurfaceVariant = KanoInkSecondaryLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = KanoOffWhiteBgLight,
    surfaceContainer = KanoSurfaceNeutralLight,
    surfaceContainerHigh = KanoSurfaceNeutralHighLight,
    surfaceContainerHighest = Color(0xFFDCD6CC),
    primaryContainer = KanoPeachContainerLight,
    onPrimaryContainer = Color(0xFF5A0E00),
    secondary = KanoPeachAccentLight,
    onSecondary = Color.Black,
    tertiaryContainer = KanoGreenContainerLight,
    secondaryContainer = KanoLavenderContainerLight,
    onSecondaryContainer = Color(0xFF21153B),
    outline = Color(0xFFE0DDD5),
    error = Color(0xFFD32F2F),
)

private val KanoDarkColors = darkColorScheme(
    primary = KanoCoralPrimaryDark,
    onPrimary = Color.White,
    background = KanoOffWhiteBgDark,
    onBackground = KanoInkPrimaryDark,
    surface = KanoSurfaceWhiteDark,
    onSurface = KanoInkPrimaryDark,
    surfaceVariant = KanoSurfaceNeutralDark,
    onSurfaceVariant = KanoInkSecondaryDark,
    surfaceContainerLowest = KanoSurfaceWhiteDark,
    surfaceContainerLow = KanoOffWhiteBgDark,
    surfaceContainer = KanoSurfaceNeutralDark,
    surfaceContainerHigh = KanoSurfaceNeutralHighDark,
    surfaceContainerHighest = Color(0xFF383838),
    primaryContainer = KanoPeachContainerDark,
    onPrimaryContainer = Color(0xFFFFD1C7),
    secondary = KanoPeachAccentDark,
    onSecondary = Color.Black,
    tertiaryContainer = KanoGreenContainerDark,
    secondaryContainer = KanoLavenderContainerDark,
    onSecondaryContainer = Color(0xFFE2DCF8),
    outline = Color(0xFF2A2A2A),
    error = Color(0xFFEF5350),
)

@Composable
fun KanoTheme(
    themeMode: KanoThemeMode = KanoThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        KanoThemeMode.LIGHT -> false
        KanoThemeMode.DARK -> true
        KanoThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colors = if (darkTheme) KanoDarkColors else KanoLightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}

@Composable
fun KanoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun KanoHeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val gradient = Brush.horizontalGradient(listOf(KanoCoralGradientStart, KanoCoralGradientEnd))
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun KanoOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun KanoCircularIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 48.dp,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
fun KanoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
fun SectionTitle(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
fun FactRow(label: String, value: String, detail: String? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        if (value.length > 20) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        } else {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
}
