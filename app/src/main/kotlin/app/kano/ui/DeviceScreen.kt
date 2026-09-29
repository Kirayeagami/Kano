package app.kano.ui

import android.content.Intent
import android.provider.Settings
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Thermostat
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceScreen(state: DeviceState, refresh: () -> Unit) {
    val context = LocalContext.current
    var showDetailedStorage by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        KanoWaveBackground(waveColor = KanoBlueContainer.copy(alpha = 0.35f))

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionTitle(
                    title = "Device Intelligence",
                    detail = "Measured on this phone using official Android System APIs. Zero fake claims.",
                )
            }

            when (state) {
                DeviceState.Loading -> item {
                    KanoGlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Spacer(Modifier.width(12.dp))
                            Text("Reading live device metrics...")
                        }
                    }
                }

                DeviceState.Failed -> item {
                    KanoGlassCard {
                        Text("Device readings unavailable. Tap refresh to retry.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        KanoOutlinedButton(onClick = refresh, modifier = Modifier.fillMaxWidth()) {
                            Text("Retry Readings")
                        }
                    }
                }

                is DeviceState.Ready -> {
                    val snapshot = state.snapshot
                    val usedBytes = snapshot.storageTotal - snapshot.storageAvailable
                    val usedPercent = if (snapshot.storageTotal > 0) {
                        ((usedBytes.toDouble() / snapshot.storageTotal) * 100).toInt()
                    } else 0

                    // 1. Premium Hero Device Header
                    item {
                        KanoGlassCard(cornerRadius = 24.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${snapshot.manufacturer.uppercase()} ${snapshot.model}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Android ${snapshot.androidVersion} · API ${snapshot.sdkInt} · Patch ${snapshot.securityPatch}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                StatusChip("● LIVE", containerColor = MaterialTheme.colorScheme.primaryContainer)
                            }

                            Spacer(Modifier.height(14.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StatusChip("Storage: $usedPercent% Used")
                                StatusChip("Battery: ${snapshot.batteryPercent ?: "--"}%")
                                StatusChip("RAM: ${snapshot.memoryAvailable?.let { Formatter.formatFileSize(context, it) } ?: "--"} Free")
                                StatusChip("Network: ${snapshot.connectionType}")
                            }
                        }
                    }

                    // 2. Storage Intelligence Card
                    item {
                        KanoGlassCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.SdCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Storage Intelligence",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                StatusChip("$usedPercent% USED")
                            }

                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "$usedPercent%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (usedPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "${Formatter.formatFileSize(context, snapshot.storageAvailable)} free of ${Formatter.formatFileSize(context, snapshot.storageTotal)} total",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Measured on internal storage volume. Kano never modifies files without explicit confirmation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KanoOutlinedButton(
                                    onClick = { showDetailedStorage = !showDetailedStorage },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(if (showDetailedStorage) "Hide Details" else "Storage Breakdown")
                                }
                                KanoButton(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                                        } catch (_: Exception) {
                                            // System settings unavailable
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("System Storage")
                                }
                            }

                            if (showDetailedStorage) {
                                Spacer(Modifier.height(10.dp))
                                FactRow("Used Storage", Formatter.formatFileSize(context, usedBytes))
                                FactRow("Available Storage", Formatter.formatFileSize(context, snapshot.storageAvailable))
                                FactRow("Total Capacity", Formatter.formatFileSize(context, snapshot.storageTotal))
                            }
                        }
                    }

                    // 3. Memory (RAM) Analytics Card
                    item {
                        KanoGlassCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Memory (RAM)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                StatusChip(if (snapshot.memoryLow) "LOW MEMORY" else "SYSTEM MANAGED")
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = snapshot.memoryAvailable?.let { Formatter.formatFileSize(context, it) + " available" } ?: "RAM snapshot unavailable",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Android OS manages RAM cache automatically; manual clearing is not a performance gain.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                            )

                            if (snapshot.memoryTotal != null) {
                                FactRow("Total RAM Capacity", Formatter.formatFileSize(context, snapshot.memoryTotal))
                                FactRow("Available RAM", Formatter.formatFileSize(context, snapshot.memoryAvailable ?: 0L))
                            }
                        }
                    }

                    // 4. Battery & Thermal Center
                    item {
                        KanoGlassCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Battery & Power",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                StatusChip(snapshot.batteryPercent?.let { "$it%" } ?: "UNKNOWN")
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = when (snapshot.charging) {
                                    true -> "Charging or Full"
                                    false -> "Discharging"
                                    null -> "Charging state unavailable"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(Modifier.height(8.dp))
                            if (snapshot.batteryTemperatureC != null) {
                                FactRow("Battery Temperature", "${snapshot.batteryTemperatureC} °C", "Normal operating range")
                            }
                            if (snapshot.batteryVoltageMv != null) {
                                FactRow("Battery Voltage", "${snapshot.batteryVoltageMv} mV")
                            }

                            Spacer(Modifier.height(8.dp))
                            KanoOutlinedButton(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                                    } catch (_: Exception) {
                                        // Settings page not available
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Android Battery Settings")
                            }
                        }
                    }

                    // 5. Connectivity & Network Status
                    item {
                        KanoGlassCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.SignalCellularAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Connectivity & Network",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                )
                                StatusChip("LOCAL ONLY")
                            }

                            Spacer(Modifier.height(8.dp))
                            FactRow("Active Network", snapshot.connectionType)
                            FactRow("Bluetooth State", snapshot.bluetoothEnabled?.let { if (it) "Enabled" else "Disabled" } ?: "Permission Not Requested")
                        }
                    }

                    // 6. Hardware & Software Specs Details
                    item {
                        KanoGlassCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Smartphone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "System & Hardware Specs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            FactRow("Manufacturer / Brand", "${snapshot.manufacturer} (${snapshot.brand})")
                            FactRow("Device Model", snapshot.model)
                            FactRow("CPU Architecture", "${snapshot.cpuAbi} (${snapshot.cpuCores} Cores)")
                            FactRow("Android Build ID", snapshot.buildId)
                            FactRow("Reading Captured At", DateFormat.getTimeInstance().format(Date(snapshot.capturedAt)))
                        }
                    }

                    // 7. Manual Refresh CTA
                    item {
                        KanoButton(
                            onClick = refresh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Refresh Device Readings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
