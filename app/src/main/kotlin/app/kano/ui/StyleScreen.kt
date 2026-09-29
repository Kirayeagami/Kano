package app.kano.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StyleScreen() {
    var isAnalyzing by remember { mutableStateOf(false) }
    var outfitAnalyzed by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        KanoWaveBackground(waveColor = KanoLavenderContainer.copy(alpha = 0.35f))

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionTitle(
                    title = "Style Studio",
                    detail = "Analyze outfits, organize your wardrobe, and identify genuine style gaps.",
                )
            }

            // Hero Capture & Analysis Card with Large Photo Viewport
            item {
                KanoGlassCard(cornerRadius = 28.dp) {
                    Text(
                        text = "Analyze Your Outfit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Upload or take a photo to evaluate color, fit, layering, and occasion suitability.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Large Photo Viewport Area with Scanning Animation Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                Color.Black.copy(alpha = 0.08f),
                                RoundedCornerShape(20.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Checkroom,
                                contentDescription = "Outfit preview area",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.height(52.dp).width(52.dp),
                            )
                            Text(
                                text = if (outfitAnalyzed) "Outfit Captured (Sample)" else "No outfit photo selected",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }

                        // Scanning Motion Line Overlay
                        if (isAnalyzing) {
                            val infiniteTransition = rememberInfiniteTransition(label = "scanAnim")
                            val scanOffsetY by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 200f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse,
                                ),
                                label = "scanY",
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .padding(top = scanOffsetY.dp)
                                    .background(KanoCoralGradientStart),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        KanoHeroButton(
                            onClick = { isAnalyzing = true; outfitAnalyzed = true },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("Take photo", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        KanoOutlinedButton(
                            onClick = { isAnalyzing = true; outfitAnalyzed = true },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Outlined.UploadFile, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Upload photo")
                        }
                    }

                    if (isAnalyzing) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth(), color = KanoCoralPrimary)
                        Text(
                            text = "Analyzing clothing composition, color, and fit...",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            // Today's Recommendation Card
            item {
                KanoGlassCard(
                    glassColor = KanoPeachContainer.copy(alpha = 0.88f),
                    cornerRadius = 24.dp,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Today · What Should I Wear?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip("RECOMMENDED")
                    }
                    Text(
                        text = "Dark Wine Overshirt + Grey Trousers + White Sneakers",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Reason: Matches cool morning weather and casual work environment.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    )
                }
            }

            // Style Analysis Breakdown
            if (outfitAnalyzed) {
                item {
                    AnimatedVisibility(
                        visible = outfitAnalyzed,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
                    ) {
                        KanoGlassCard {
                            Text(
                                text = "Outfit Evaluation Criteria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(8.dp))
                            ScoreRow("Color Coordination", "8/10", "Strong contrast between wine overshirt and neutral trousers.")
                            ScoreRow("Fit Consistency", "7/10", "Top silhouette fits well; trousers slightly long.")
                            ScoreRow("Layering", "8/10", "Effective inner tee and overshirt combination.")
                            ScoreRow("Occasion Suitability", "9/10", "Ideal for casual work, study, or daily meetings.")
                        }
                    }
                }
            }

            // Wardrobe Catalog Overview
            item {
                KanoGlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wardrobe Catalog",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "24 total items cataloged",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        StatusChip("24 ITEMS")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        WardrobeCategoryTile("Tops", "8")
                        WardrobeCategoryTile("Bottoms", "6")
                        WardrobeCategoryTile("Outerwear", "4")
                        WardrobeCategoryTile("Shoes", "4")
                        WardrobeCategoryTile("Other", "2")
                    }
                }
            }

            // Shopping & Wardrobe Gaps Card
            item {
                KanoGlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Shopping & Wardrobe Gaps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip("2 GAPS")
                    }
                    Text(
                        text = "Before buying, Kano checks existing inventory to prevent duplicate purchases.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GapRow(
                        title = "Neutral Overshirt",
                        reason = "Unlocks 5 new outfit combinations with existing dark trousers.",
                        priority = "NEED SOON",
                    )
                    GapRow(
                        title = "Minimal White Leather Sneakers",
                        reason = "Replaces worn footwear for smart-casual wear.",
                        priority = "OPTIONAL",
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(title: String, score: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = score,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WardrobeCategoryTile(label: String, count: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GapRow(title: String, reason: String, priority: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        StatusChip(priority)
    }
}
