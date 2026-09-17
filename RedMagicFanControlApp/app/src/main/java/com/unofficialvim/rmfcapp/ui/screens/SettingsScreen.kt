package com.unofficialvim.rmfcapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unofficialvim.rmfcapp.BuildConfig
import com.unofficialvim.rmfcapp.R
import com.unofficialvim.rmfcapp.net.DomainSocketClient
import kotlinx.coroutines.launch

/**
 * Second page: a list-style settings screen, reached via the gear icon
 * on HomeScreen. Appearance toggles apply immediately (in-memory only -
 * wire up DataStore if they need to survive process death). The
 * "Reconnect" row exercises the DomainSocketClient stub end to end.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    darkThemeOverride: Boolean?,
    onDarkThemeOverrideChange: (Boolean?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var connectionStatus by remember { mutableStateOf(context.getString(R.string.status_idle)) }
    val socketPath = remember { DomainSocketClient.defaultSocketPath(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_cd)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item { SectionHeader(stringResource(R.string.section_appearance)) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.pref_dynamic_color)) },
                    supportingContent = { Text(stringResource(R.string.pref_dynamic_color_desc)) },
                    leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) },
                    trailingContent = {
                        Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChange)
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.pref_dark_theme)) },
                    supportingContent = { Text(stringResource(R.string.pref_dark_theme_desc)) },
                    trailingContent = {
                        Switch(
                            checked = darkThemeOverride == true,
                            onCheckedChange = { checked -> onDarkThemeOverrideChange(checked) }
                        )
                    }
                )
            }

            item { SectionHeader(stringResource(R.string.section_connection)) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.pref_socket_path)) },
                    supportingContent = { Text(socketPath) },
                    leadingContent = { Icon(Icons.Filled.Router, contentDescription = null) }
                )
            }

            item {
                ListItem(
                    modifier = Modifier.clickable {
                        scope.launch {
                            connectionStatus = "Connecting..."
                            val client = DomainSocketClient(socketName = socketPath)
                            val result = client.connect()
                            connectionStatus = if (result.isSuccess) {
                                "Connected"
                            } else {
                                "Failed: ${result.exceptionOrNull()?.message}"
                            }
                            client.disconnect()
                        }
                    },
                    headlineContent = { Text(stringResource(R.string.pref_reconnect)) },
                    supportingContent = { Text(connectionStatus) },
                    leadingContent = { Icon(Icons.Filled.Sync, contentDescription = null) }
                )
            }

            item { SectionHeader(stringResource(R.string.section_about)) }

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.pref_version)) },
                    supportingContent = { Text(BuildConfig.VERSION_NAME) },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}
