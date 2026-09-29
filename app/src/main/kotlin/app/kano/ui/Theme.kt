package app.kano.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// --- Bright Mode Palette ---
val KanoCoralPrimaryLight = Color(0xFFE84A27)
val KanoCoralGradientStartLight = Color(0xFFFF5A36)
val KanoCoralGradientEndLight = Color(0xFFD8391A)
val KanoOffWhiteBgLight = Color(0xFFFAF8F5)
val KanoSurfaceWhiteLight = Color(0xFFFFFFFF)
val KanoSurfaceNeutralLight = Color(0xFFF3F0EC)
val KanoSurfaceNeutralHighLight = Color(0xFFEBE6E0)
val KanoInkPrimaryLight = Color(0xFF121212)
val KanoInkSecondaryLight = Color(0xFF666666)
val KanoPeachContainerLight = Color(0xFFFFE8E0)
val KanoLavenderContainerLight = Color(0xFFE8E5F8)
val KanoGreenContainerLight = Color(0xFFE8F5E9)

// --- Dark Mode Palette ---
val KanoCoralPrimaryDark = Color(0xFFFF6C4B)
val KanoCoralGradientStartDark = Color(0xFFFF7A5C)
val KanoCoralGradientEndDark = Color(0xFFE04224)
val KanoOffWhiteBgDark = Color(0xFF141210)
val KanoSurfaceWhiteDark = Color(0xFF1E1C18)
val KanoSurfaceNeutralDark = Color(0xFF282520)
val KanoSurfaceNeutralHighDark = Color(0xFF322E28)
val KanoInkPrimaryDark = Color(0xFFF7F4F0)
val KanoInkSecondaryDark = Color(0xFF9E9790)
val KanoPeachContainerDark = Color(0xFF3D1B13)
val KanoLavenderContainerDark = Color(0xFF2A263B)
val KanoGreenContainerDark = Color(0xFF1C3322)

// Dynamic Color Holders for Motion/Wave Systems
var KanoPeachContainer = KanoPeachContainerLight
var KanoLavenderContainer = KanoLavenderContainerLight
var KanoGreenContainer = KanoGreenContainerLight
var KanoCoralGradientStart = KanoCoralGradientStartLight
var KanoCoralGradientEnd = KanoCoralGradientEndLight
var KanoCoralPrimary = KanoCoralPrimaryLight

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
    surfaceContainerHighest = Color(0xFFE0DAD2),
    primaryContainer = KanoPeachContainerLight,
    onPrimaryContainer = Color(0xFF5A1407),
    secondary = Color(0xFF4A3E3D),
    onSecondary = Color.White,
    secondaryContainer = KanoLavenderContainerLight,
    onSecondaryContainer = Color(0xFF231C38),
    outline = Color(0xFFD4CDC5),
    error = Color(0xFFC62828),
)

private val KanoDarkColors = darkColorScheme(
    primary = KanoCoralPrimaryDark,
    onPrimary = Color(0xFF2A0A04),
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
    surfaceContainerHighest = Color(0xFF3A352F),
    primaryContainer = KanoPeachContainerDark,
    onPrimaryContainer = Color(0xFFFFD0C5),
    secondary = Color(0xFFD4C2BD),
    onSecondary = Color(0xFF2A201E),
    secondaryContainer = KanoLavenderContainerDark,
    onSecondaryContainer = Color(0xFFE2DCF8),
    outline = Color(0xFF48423B),
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

    if (darkTheme) {
        KanoPeachContainer = KanoPeachContainerDark
        KanoLavenderContainer = KanoLavenderContainerDark
        KanoGreenContainer = KanoGreenContainerDark
        KanoCoralGradientStart = KanoCoralGradientStartDark
        KanoCoralGradientEnd = KanoCoralGradientEndDark
        KanoCoralPrimary = KanoCoralPrimaryDark
    } else {
        KanoPeachContainer = KanoPeachContainerLight
        KanoLavenderContainer = KanoLavenderContainerLight
        KanoGreenContainer = KanoGreenContainerLight
        KanoCoralGradientStart = KanoCoralGradientStartLight
        KanoCoralGradientEnd = KanoCoralGradientEndLight
        KanoCoralPrimary = KanoCoralPrimaryLight
    }

    val colors = if (darkTheme) KanoDarkColors else KanoLightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(16.dp))
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
        shape = RoundedCornerShape(16.dp),
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
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
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
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
}
