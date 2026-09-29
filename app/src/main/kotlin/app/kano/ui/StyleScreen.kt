package app.kano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StyleScreen() {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("Style Studio", "A wardrobe built from what you actually own.") }
        item {
            KanoGlassCard(glassColor = MaterialTheme.colorScheme.secondaryContainer) {
                Text("Not available yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("Photo analysis, wardrobe storage and outfit recommendations have not been implemented.",
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        item {
            KanoGlassCard {
                Text("No photo has been analyzed", style = MaterialTheme.typography.titleLarge)
                Text("Kano will not invent clothing scores, weather, wardrobe items or shopping gaps. Camera and upload actions will appear when their processing and storage are ready.")
            }
        }
    }
}
