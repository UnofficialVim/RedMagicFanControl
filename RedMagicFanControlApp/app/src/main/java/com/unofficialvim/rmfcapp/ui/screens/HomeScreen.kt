package com.unofficialvim.rmfcapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unofficialvim.rmfcapp.R
import com.unofficialvim.rmfcapp.data.DeviceReadings

private data class Reading(
    val label: String,
    val value: String,
    val icon: ImageVector
)

/**
 * First page: a handful of informational device readings.
 *
 * Battery/memory/storage are read live from cheap platform APIs. The
 * daemon-backed reading is a stub - it will show real data once
 * DomainSocketClient is wired up to the daemon's actual protocol.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSettingsClick: () -> Unit) {
    val context = LocalContext.current

    val readings = remember {
        listOf(
            Reading(
                label = "Battery",
                value = DeviceReadings.batteryPercent(context)?.let { "$it%" } ?: "--",
                icon = Icons.Filled.BatteryFull
            ),
            Reading(
                label = "Available memory",
                value = DeviceReadings.availableMemoryMb(context)?.let { "$it MB" } ?: "--",
                icon = Icons.Filled.Memory
            ),
            Reading(
                label = "Free storage",
                value = DeviceReadings.freeStorageMb(context)?.let { "$it MB" } ?: "--",
                icon = Icons.Filled.Storage
            ),
            Reading(
                label = "Daemon status",
                value = "Not connected",
                icon = Icons.Filled.DeviceThermostat
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_cd)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(readings) { reading ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(reading.label) },
                        supportingContent = {
                            Text(reading.value, style = MaterialTheme.typography.titleMedium)
                        },
                        leadingContent = { Icon(reading.icon, contentDescription = null) }
                    )
                }
            }
        }
    }
}
