package com.unofficialvim.rmfcapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unofficialvim.rmfcapp.ui.screens.HomeScreen
import com.unofficialvim.rmfcapp.ui.screens.SettingsScreen
import com.unofficialvim.rmfcapp.ui.theme.RMFCTheme

private object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // In-memory app settings. Swap for a DataStore-backed source
            // once these need to survive process death.
            var dynamicColor by remember { mutableStateOf(true) }
            // null = follow the system light/dark setting.
            var darkThemeOverride by remember { mutableStateOf<Boolean?>(null) }

            RMFCTheme(
                darkTheme = darkThemeOverride ?: isSystemInDarkTheme(),
                dynamicColor = dynamicColor
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        HomeScreen(onSettingsClick = { navController.navigate(Routes.SETTINGS) })
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            dynamicColor = dynamicColor,
                            onDynamicColorChange = { dynamicColor = it },
                            darkThemeOverride = darkThemeOverride,
                            onDarkThemeOverrideChange = { darkThemeOverride = it },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
