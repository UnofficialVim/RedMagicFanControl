package com.unofficialvim.redmagicfancontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unofficialvim.redmagicfancontrol.data.ThemeMode
import com.unofficialvim.redmagicfancontrol.ui.screens.HomeScreen
import com.unofficialvim.redmagicfancontrol.ui.screens.SettingsScreen
import com.unofficialvim.redmagicfancontrol.ui.theme.RMFCTheme
import kotlinx.coroutines.launch

private object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { vm.poll() } }
        setContent {
            val s by vm.state.collectAsState()
            RMFCTheme(
                darkTheme = when (s.theme) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
            ) {
                val nav = rememberNavController()
                NavHost(nav, startDestination = Routes.HOME) {
                    composable(Routes.HOME) { HomeScreen(s, vm) { nav.navigate(Routes.SETTINGS) } }
                    composable(Routes.SETTINGS) { SettingsScreen(s, vm) { nav.popBackStack() } }
                }
            }
        }
    }
}
