package app.kano.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val KanoColors = lightColorScheme(
    primary = Color(0xFF481526), onPrimary = Color(0xFFFFF8F3),
    background = Color(0xFFF3F0E6), onBackground = Color(0xFF292322),
    surface = Color(0xFFF3F0E6), onSurface = Color(0xFF292322),
    surfaceVariant = Color(0xFFE8E2D7), onSurfaceVariant = Color(0xFF60534F),
    surfaceContainerLowest = Color(0xFFFFFBF2), surfaceContainerLow = Color(0xFFF5F0E6),
    surfaceContainer = Color(0xFFEFE9DF), surfaceContainerHigh = Color(0xFFECE6DB),
    surfaceContainerHighest = Color(0xFFE4DDD2),
    primaryContainer = Color(0xFFEEDCE1), onPrimaryContainer = Color(0xFF38101C),
    secondary = Color(0xFF675744), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBDFC8), onSecondaryContainer = Color(0xFF292322),
    outline = Color(0xFF81736B), error = Color(0xFF9C2222),
)

@Composable fun KanoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = KanoColors, typography = Typography(), shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp), small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp), large = RoundedCornerShape(8.dp),
        extraLarge = RoundedCornerShape(8.dp),
    ), content = content)
}

@Composable fun KanoButton(onClick: () -> Unit, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    Button(onClick = onClick, enabled = enabled, shape = MaterialTheme.shapes.small, content = content)
}

@Composable fun KanoOutlinedButton(onClick: () -> Unit, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, shape = MaterialTheme.shapes.small, content = content)
}

@Composable fun SectionTitle(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Text(detail, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable fun FactRow(label: String, value: String, detail: String? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 4.dp))
        if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}
