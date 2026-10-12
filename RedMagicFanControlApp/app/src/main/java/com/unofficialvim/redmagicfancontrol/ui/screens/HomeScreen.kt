package com.unofficialvim.redmagicfancontrol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unofficialvim.redmagicfancontrol.MainViewModel
import com.unofficialvim.redmagicfancontrol.R
import com.unofficialvim.redmagicfancontrol.UiState
import com.unofficialvim.redmagicfancontrol.data.MAX_PROFILES
import com.unofficialvim.redmagicfancontrol.data.Profile

private val FULL = GridItemSpan(2)

/** Overview: stat tiles grouped by hardware (cooling / processor / daemon), then the profile list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: UiState, vm: MainViewModel, onSettingsClick: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    // null = closed; Pair(old, _) = editing old (old == null means adding)
    var editing by remember { mutableStateOf<Pair<Profile?, Boolean>?>(null) }
    var deleting by remember { mutableStateOf<Profile?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); vm.messageShown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, stringResource(R.string.settings_cd))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(pad),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            header("Cooling")
            item {
                val lvl = state.fanLevel
                Tile(Icons.Filled.Air, "Fan speed", lvl?.let { "$it / 5" } ?: "--") {
                    LinearProgressIndicator(progress = { (lvl ?: 0) / 5f }, modifier = Modifier.fillMaxWidth())
                    Text(if (lvl == 0) "Off" else if (lvl == 5) "Max" else "", style = MaterialTheme.typography.labelMedium)
                }
            }
            item {
                val on = state.config?.callsSilenceFan
                Tile(
                    Icons.Filled.Call, "Fan during calls",
                    when (on) { true -> "Off"; false -> "On"; null -> "--" }
                ) { Text(if (on == true) "Silenced while on a call" else "", style = MaterialTheme.typography.labelMedium) }
            }

            header("Processor")
            item(span = { FULL }) {
                Tile(Icons.Filled.DeviceThermostat, "CPU temperature", state.cpuTempC?.let { "%.1f °C".format(it) } ?: "--")
            }

            header("Daemon")
            item {
                Tile(
                    if (state.connected) Icons.Filled.Link else Icons.Filled.LinkOff,
                    "Connection", if (state.connected) "Connected" else "Disconnected"
                ) { state.connectError?.let { Text(it, style = MaterialTheme.typography.labelSmall, maxLines = 3) } }
            }
            item { Tile(Icons.Filled.Tune, "Active profile", state.activeProfile ?: "--") }

            header("Profiles", action = {
                IconButton(
                    onClick = { editing = null to true },
                    enabled = state.config != null && state.config.profiles.size < MAX_PROFILES,
                ) { Icon(Icons.Filled.Add, "Add profile") }
            })
            val cfg = state.config
            if (cfg == null) item(span = { FULL }) { Text("Config not loaded. Check the config path in Settings.") }
            else items(cfg.profiles, span = { FULL }) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    ListItem(
                        headlineContent = { Text(p.name) },
                        supportingContent = { Text("${p.steps.size} steps · refresh ${p.refreshRate}s") },
                        leadingContent = { RadioButton(selected = p.name == cfg.activeProfile, onClick = { vm.setActive(p) }) },
                        trailingContent = {
                            Row {
                                IconButton({ editing = p to true }) { Icon(Icons.Filled.Edit, "Edit ${p.name}") }
                                IconButton({ deleting = p }, enabled = cfg.profiles.size > 1) { Icon(Icons.Filled.Delete, "Delete ${p.name}") }
                            }
                        },
                        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    )
                }
            }
        }
    }

    editing?.let { (old, _) ->
        ProfileEditor(
            old = old,
            existingNames = state.config?.profiles?.map { it.name }.orEmpty() - listOfNotNull(old?.name).toSet(),
            onDismiss = { editing = null },
            onSave = { vm.saveProfile(old, it); editing = null },
        )
    }
    deleting?.let { p ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete \"${p.name}\"?") },
            confirmButton = {
                androidx.compose.material3.TextButton({ vm.deleteProfile(p); deleting = null }) { Text("Delete") }
            },
            dismissButton = { androidx.compose.material3.TextButton({ deleting = null }) { Text("Cancel") } },
        )
    }
}

private fun LazyGridScope.header(title: String, action: @Composable () -> Unit = {}) {
    item(span = { FULL }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            action()
        }
    }
}

@Composable
private fun Tile(icon: ImageVector, label: String, value: String, extra: @Composable () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall)
            extra()
        }
    }
}
