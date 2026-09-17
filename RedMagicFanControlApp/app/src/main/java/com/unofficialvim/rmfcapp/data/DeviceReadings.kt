package com.unofficialvim.rmfcapp.data

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.StatFs

/**
 * Lightweight, synchronous device reading helpers.
 *
 * These read cheap local Android APIs directly and need no permissions.
 * Anything that has to come from the daemon (fan speed, board temperature,
 * etc.) is left as a stub in the UI layer until DomainSocketClient speaks
 * a real protocol - see net/DomainSocketClient.kt.
 */
object DeviceReadings {

    fun batteryPercent(context: Context): Int? {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val value = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        return value.takeIf { it in 0..100 }
    }

    fun availableMemoryMb(context: Context): Long? {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return info.availMem / (1024 * 1024)
    }

    fun freeStorageMb(context: Context): Long? {
        return try {
            val stat = StatFs(context.filesDir.path)
            stat.availableBytes / (1024 * 1024)
        } catch (e: Exception) {
            null
        }
    }
}
