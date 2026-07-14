package com.example.redmagicfancontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.example.redmagicfancontrol.ui.theme.RedMagicFanControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedMagicFanControlTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var showSettings by remember { mutableStateOf(false) }

    // Let the system back button/gesture return from Settings instead of closing the app.
    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
    } else {
        MainScreen(onSettingsClick = { showSettings = true })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSettingsClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RedMagicFanControl") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(painterResource(R.drawable.settings_24px), contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        var counter by remember { mutableStateOf(0) }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.chevron_backward_24px), contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        var darkMode by remember { mutableStateOf(false) }
        var notifications by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            SettingRow("Dark mode", darkMode) { darkMode = it }
            SettingRow("Notifications", notifications) { notifications = it }
        }
    }
}

@Composable
fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}