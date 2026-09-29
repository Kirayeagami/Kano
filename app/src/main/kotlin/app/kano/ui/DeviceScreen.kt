package app.kano.ui

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable
fun DeviceScreen(state: DeviceState, refresh: () -> Unit) {
    val context = LocalContext.current
    var showDetailedUsage by remember { mutableStateOf(false) }
    var showProcessInfo by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionTitle("Device Intelligence", "Measured on this device using official Android APIs.") }

        when (state) {
            DeviceState.Loading -> item {
                KanoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator()
                        Spacer(Modifier.width(12.dp))
                        Text("Reading device information...")
                    }
                }
            }

            DeviceState.Failed -> item {
                KanoCard {
                    Text("Device readings unavailable. Tap refresh to request a fresh snapshot.")
                }
            }

            is DeviceState.Ready -> {
                val data = state.snapshot
                val usedBytes = data.storageTotal - data.storageAvailable
                val usedPercent = if (data.storageTotal > 0) ((usedBytes.toDouble() / data.storageTotal) * 100).toInt() else 0

                // Premium Storage Card with Oversized Percentage
                item {
                    KanoCard(cornerRadius = 24.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.SdCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Storage Volume",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            StatusChip("$usedPercent% USED")
                        }

                        // Oversized Large Display Number
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "$usedPercent%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (usedPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "${Formatter.formatFileSize(context, data.storageAvailable)} free of ${Formatter.formatFileSize(context, data.storageTotal)} total",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Measured on the internal storage volume containing Kano. Not a scan of your private files.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                        )
                        KanoOutlinedButton(
                            onClick = { showDetailedUsage = !showDetailedUsage },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (showDetailedUsage) "Hide detailed usage" else "Detailed usage breakdown")
                        }

                        if (showDetailedUsage) {
                            Spacer(Modifier.height(8.dp))
                            FactRow("Used Volume", Formatter.formatFileSize(context, usedBytes))
                            FactRow("Available Storage", Formatter.formatFileSize(context, data.storageAvailable))
                        }
                    }
                }

                // Memory (RAM) Analytics Card
                item {
                    KanoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Memory (RAM)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            StatusChip("SNAPSHOT")
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = data.memoryAvailable?.let { Formatter.formatFileSize(context, it) + " available" } ?: "Memory snapshot unavailable",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "System snapshot. Android manages cached memory automatically; clearing RAM is not a performance gain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                        )
                        KanoOutlinedButton(
                            onClick = { showProcessInfo = !showProcessInfo },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (showProcessInfo) "Hide process information" else "Process information")
                        }

                        if (showProcessInfo) {
                            Spacer(Modifier.height(8.dp))
                            FactRow("Memory Policy", "Android OS Automatic Cache Management")
                            FactRow("App Sandbox", "Kano processes memory off main thread in background")
                        }
                    }
                }

                // Battery Card
                item {
                    KanoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.BatteryChargingFull,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Battery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            StatusChip(data.batteryPercent?.let { "$it%" } ?: "UNKNOWN")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = when (data.charging) {
                                true -> "Charging or full"
                                false -> "Not charging"
                                null -> "Charging state unavailable"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Attention Section (2 items worth checking)
                item {
                    KanoCard(backgroundColor = MaterialTheme.colorScheme.primaryContainer) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Attention Needed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f),
                            )
                            StatusChip("2 ITEMS")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "1. Storage volume is $usedPercent% full. Consider reviewing large media files.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "2. Selected media index has queued items waiting for scanning.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                // Specs Card
                item {
                    KanoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Smartphone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Device Specs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        FactRow("Device Model", data.model)
                        FactRow("Android Version", data.androidVersion)
                        FactRow("Captured At", DateFormat.getTimeInstance().format(Date(data.capturedAt)))
                    }
                }
            }
        }

        item {
            KanoButton(
                onClick = refresh,
                enabled = state != DeviceState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Refresh device readings")
            }
        }
    }
}
