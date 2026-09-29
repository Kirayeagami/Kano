package app.kano.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleScreen() {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionTitle("Style Studio", "A wardrobe built from what you actually own.") }

        // Camera Scan & Outfit Framing Viewport (Inspired by Reference Images)
        item {
            KanoGlassCard(cornerRadius = 24.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFB03A), Color(0xFFFF3B1D)),
                            ),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp),
                ) {
                    // Top Status Badge
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.align(Alignment.TopCenter),
                    ) {
                        Text(
                            text = "Style Scanning Ready",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }

                    // Camera Framing Box Overlay
                    Box(
                        modifier = Modifier
                            .size(width = 160.dp, height = 110.dp)
                            .align(Alignment.Center)
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(16.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Center outfit in frame",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KanoHeroButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.Camera, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Take Photo", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    KanoOutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Upload Photo")
                    }
                }
            }
        }

        // Today's Outfit Card
        item {
            KanoGlassCard(glassColor = KanoPeachContainer.copy(alpha = 0.88f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Checkroom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Today's Outfit Recommendation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip("RECOMMENDED")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Dark Wine Overshirt + Grey Tailored Trousers + White Leather Sneakers",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Based on your owned wardrobe inventory and local color coordination analysis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        // Criteria Evaluation Matrix
        item {
            KanoGlassCard {
                Text(
                    text = "Style Criteria Evaluation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                FactRow("Color Coordination", "8 / 10", "High contrast neutral balance")
                FactRow("Fit Consistency", "7 / 10", "Relaxed top with tailored bottom")
                FactRow("Layering Harmony", "8 / 10", "Overshirt layer over crewneck base")
                FactRow("Occasion Match", "9 / 10", "Casual / Smart Everyday")
            }
        }

        // Wardrobe Summary
        item {
            KanoGlassCard {
                Text(
                    text = "My Owned Wardrobe Catalog",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "24 items recorded in local wardrobe inventory.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusChip("6 Tops")
                    StatusChip("5 Bottoms")
                    StatusChip("4 Outerwear")
                    StatusChip("4 Shoes")
                    StatusChip("5 Accessories")
                }
            }
        }
    }
}
