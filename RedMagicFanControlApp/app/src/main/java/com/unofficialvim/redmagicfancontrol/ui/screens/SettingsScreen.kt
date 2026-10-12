package com.unofficialvim.redmagicfancontrol.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.unofficialvim.redmagicfancontrol.net.Diagnostics
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unofficialvim.redmagicfancontrol.MainViewModel
import com.unofficialvim.redmagicfancontrol.R
import com.unofficialvim.redmagicfancontrol.UiState
import com.unofficialvim.redmagicfancontrol.data.ThemeMode

/** List-style settings: theme dropdown plus the two locations the app reads from. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: UiState, vm: MainViewModel, onBackClick: () -> Unit) {
    var themeOpen by remember { mutableStateOf(false) }
    var editPath by remember { mutableStateOf<String?>(null) } // "config" | "socket"
    var report by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_cd))
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad)) {
            item {
                ListItem(
                    headlineContent = { Text("Theme") },
                    leadingContent = { Icon(Icons.Filled.Palette, null) },
                    trailingContent = {
                        ExposedDropdownMenuBox(expanded = themeOpen, onExpandedChange = { themeOpen = it }) {
                            Text(
                                state.theme.label() + " ▾",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.menuAnchor().padding(8.dp),
                            )
                            ExposedDropdownMenu(expanded = themeOpen, onDismissRequest = { themeOpen = false }) {
                                ThemeMode.entries.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it.label()) },
                                        onClick = { vm.setTheme(it); themeOpen = false },
                                    )
                                }
                            }
                        }
                    }
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable { editPath = "config" },
                    headlineContent = { Text("Config file") },
                    supportingContent = { Text(state.configPath) },
                    leadingContent = { Icon(Icons.Filled.Description, null) },
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable {
                        report = "Running..."
                        scope.launch { report = Diagnostics.run(state.socketPath, state.configPath) }
                    },
                    headlineContent = { Text("Connection test") },
                    supportingContent = { Text("Check socket, root and config access") },
                    leadingContent = { Icon(Icons.Filled.BugReport, null) },
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable { editPath = "socket" },
                    headlineContent = { Text("Daemon socket") },
                    supportingContent = { Text(state.socketPath) },
                    leadingContent = { Icon(Icons.Filled.Router, null) },
                )
            }
        }
    }

    report?.let { r ->
        AlertDialog(
            onDismissRequest = { report = null },
            title = { Text("Connection test") },
            text = {
                SelectionContainer {
                    Text(r, style = MaterialTheme.typography.bodySmall, modifier = Modifier.verticalScroll(rememberScrollState()))
                }
            },
            confirmButton = { TextButton({ report = null }) { Text("Close") } },
        )
    }

    editPath?.let { which ->
        var text by remember { mutableStateOf(if (which == "config") state.configPath else state.socketPath) }
        AlertDialog(
            onDismissRequest = { editPath = null },
            title = { Text(if (which == "config") "Config file" else "Daemon socket") },
            text = { OutlinedTextField(text, { text = it }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = text.isNotBlank(), onClick = {
                    if (which == "config") vm.setConfigPath(text.trim()) else vm.setSocketPath(text.trim())
                    editPath = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton({ editPath = null }) { Text("Cancel") } },
        )
    }
}

private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> "Default"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}
