package com.unofficialvim.redmagicfancontrol.data

import android.content.Context

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Plain SharedPreferences; three string values don't need DataStore. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var theme: ThemeMode
        get() = ThemeMode.entries.firstOrNull { it.name == sp.getString("theme", null) } ?: ThemeMode.SYSTEM
        set(v) = sp.edit().putString("theme", v.name).apply()

    // ponytail: guessed Magisk module locations; user can change them in Settings.
    var configPath: String
        get() = sp.getString("config", DEFAULT_CONFIG)!!
        set(v) = sp.edit().putString("config", v).apply()

    var socketPath: String
        get() = sp.getString("socket", DEFAULT_SOCKET)!!
        set(v) = sp.edit().putString("socket", v).apply()

    companion object {
        const val DEFAULT_CONFIG = "/data/adb/modules/redmagic-fan-control/config/config.json"
        const val DEFAULT_SOCKET = "/data/adb/modules/redmagic-fan-control/rmfc_socket"
    }
}
