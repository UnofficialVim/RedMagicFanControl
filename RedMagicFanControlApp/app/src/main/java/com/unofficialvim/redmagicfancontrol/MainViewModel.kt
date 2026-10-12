package com.unofficialvim.redmagicfancontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unofficialvim.redmagicfancontrol.data.Config
import com.unofficialvim.redmagicfancontrol.data.ConfigStore
import com.unofficialvim.redmagicfancontrol.data.Prefs
import com.unofficialvim.redmagicfancontrol.data.Profile
import com.unofficialvim.redmagicfancontrol.data.ThemeMode
import com.unofficialvim.redmagicfancontrol.net.DaemonClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val connected: Boolean = false,
    val connectError: String? = null,
    val fanLevel: Int? = null,
    val cpuTempC: Float? = null,
    val activeProfile: String? = null,
    val config: Config? = null,
    val message: String? = null,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val configPath: String = "",
    val socketPath: String = "",
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private var client = DaemonClient(prefs.socketPath)
    val state = MutableStateFlow(
        UiState(theme = prefs.theme, configPath = prefs.configPath, socketPath = prefs.socketPath)
    )

    init { loadConfig() }

    /** Call from a STARTED-scoped coroutine; polls until cancelled. */
    suspend fun poll() {
        while (true) {
            try {
                val temp = client.request("getCpuTemp").trim().toInt() / 1000f
                val fan = client.request("getFanSpeed").trim().toInt()
                val profile = client.request("getActiveProfile").trim()
                state.update { it.copy(connected = true, connectError = null, cpuTempC = temp, fanLevel = fan, activeProfile = profile) }
            } catch (e: Exception) { // IOException or non-numeric reply
                state.update { it.copy(connected = false, connectError = e.message ?: e.toString(), cpuTempC = null, fanLevel = null, activeProfile = null) }
            }
            delay(2000)
        }
    }

    fun loadConfig() = io {
        val c = Config.parse(ConfigStore.read(prefs.configPath))
        state.update { it.copy(config = c) }
    }

    /** Persists [updated] to the config file, then asks the daemon to reload it. */
    fun saveConfig(updated: Config) = io {
        ConfigStore.write(prefs.configPath, updated.toJson())
        state.update { it.copy(config = updated) }
        client.request("reloadConfig")
    }

    fun saveProfile(old: Profile?, new: Profile) {
        val c = state.value.config ?: return
        val list = if (old == null) c.profiles + new else c.profiles.map { if (it == old) new else it }
        saveConfig(c.copy(profiles = list, activeProfile = if (old?.name == c.activeProfile) new.name else c.activeProfile))
    }

    fun deleteProfile(p: Profile) {
        val c = state.value.config ?: return
        val rest = c.profiles - p
        saveConfig(c.copy(profiles = rest, activeProfile = if (p.name == c.activeProfile) rest.firstOrNull()?.name ?: "" else c.activeProfile))
    }

    fun setActive(p: Profile) = io {
        val c = state.value.config
        if (c != null) { // keep the file in sync so a later reload doesn't revert it
            ConfigStore.write(prefs.configPath, c.copy(activeProfile = p.name).toJson())
            state.update { it.copy(config = c.copy(activeProfile = p.name)) }
        }
        client.request("setActiveProfile ${p.name}")
    }

    fun setTheme(t: ThemeMode) { prefs.theme = t; state.update { it.copy(theme = t) } }

    fun setConfigPath(p: String) {
        prefs.configPath = p
        state.update { it.copy(configPath = p) }
        loadConfig()
    }

    fun setSocketPath(p: String) {
        prefs.socketPath = p
        state.update { it.copy(socketPath = p) }
        client.close()
        client = DaemonClient(p)
    }

    fun messageShown() = state.update { it.copy(message = null) }

    private fun io(block: suspend () -> Unit) {
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { block() } }
            catch (e: Exception) { state.update { it.copy(message = e.message ?: e.toString()) } }
        }
    }
}
