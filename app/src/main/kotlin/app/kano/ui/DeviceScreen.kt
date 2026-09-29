package app.kano.ui

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import app.kano.ui.KanoButton as Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable fun DeviceScreen(state: DeviceState, refresh: () -> Unit) {
    val context = LocalContext.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SectionTitle("Device", "Measured on this device. No access to your files is needed.") }
        when (state) {
            DeviceState.Loading -> item { CircularProgressIndicator(); Text("Reading device information…") }
            DeviceState.Failed -> item { Text("Device readings unavailable. Retry to request a fresh snapshot.") }
            is DeviceState.Ready -> {
                val data = state.snapshot
                item { FactRow("Device", data.model, data.androidVersion) }
                item { FactRow("Available storage", Formatter.formatFileSize(context, data.storageAvailable),
                    "Of ${Formatter.formatFileSize(context, data.storageTotal)} on the volume containing Kano. Not a scan of your files.") }
                item { FactRow("Available memory", data.memoryAvailable?.let { Formatter.formatFileSize(context, it) } ?: "Unavailable",
                    "System snapshot. Android manages cached memory; freeing RAM is not a performance score.") }
                item { FactRow("Battery", data.batteryPercent?.let { "$it%" } ?: "Unavailable",
                    when (data.charging) { true -> "Charging or full"; false -> "Not charging"; null -> "Charging state unavailable" }) }
                item { Text("Read at ${DateFormat.getTimeInstance().format(Date(data.capturedAt))}. Values change over time.") }
            }
        }
        item { Button(onClick = refresh, enabled = state != DeviceState.Loading) { Text("Refresh readings") } }
    }
}
