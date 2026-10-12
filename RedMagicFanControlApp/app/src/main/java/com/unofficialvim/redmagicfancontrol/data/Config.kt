package com.unofficialvim.redmagicfancontrol.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

// Limits mirrored from the daemon's config.h
const val MAX_PROFILES = 8
const val MAX_STEPS = 16
const val MAX_NAME = 31
const val MAX_LEVEL = 5

/** [tempMilliC] is stored as in the daemon config (40000 = 40 °C). */
data class Step(val tempMilliC: Int, val fanLvl: Int)

data class Profile(val name: String, val refreshRate: Double, val steps: List<Step>)

/**
 * The daemon's config.json. [raw] keeps keys the app doesn't edit
 * (paths, log level, ...) so saving never drops them.
 */
data class Config(
    val raw: JSONObject,
    val callsSilenceFan: Boolean,
    val activeProfile: String,
    val profiles: List<Profile>,
) {
    fun toJson(): String {
        val o = JSONObject(raw.toString())
        o.put("calls_silence_fan", callsSilenceFan)
        o.put("active_profile", activeProfile)
        o.put("profiles", JSONArray().apply {
            profiles.forEach { p ->
                put(JSONObject().apply {
                    put("name", p.name)
                    put("refresh_rate", p.refreshRate)
                    put("fan_curve", JSONObject().put("steps", JSONArray().apply {
                        // daemon walks steps in order and stops at the first one above the temp
                        p.steps.sortedBy { it.tempMilliC }.forEach {
                            put(JSONObject().put("temp_c", it.tempMilliC).put("fan_lvl", it.fanLvl))
                        }
                    }))
                })
            }
        })
        return o.toString(4)
    }

    companion object {
        fun parse(text: String): Config {
            val o = JSONObject(text)
            val ps = o.optJSONArray("profiles") ?: JSONArray()
            val profiles = (0 until ps.length()).map { ps.getJSONObject(it) }.map { p ->
                val st = p.optJSONObject("fan_curve")?.optJSONArray("steps") ?: JSONArray()
                Profile(
                    p.getString("name"),
                    p.optDouble("refresh_rate", 5.0),
                    (0 until st.length()).map { st.getJSONObject(it) }
                        .map { Step(it.getInt("temp_c"), it.getInt("fan_lvl")) },
                )
            }
            return Config(
                o,
                o.optBoolean("calls_silence_fan", true),
                o.optString("active_profile", profiles.firstOrNull()?.name ?: ""),
                profiles,
            )
        }
    }
}

/**
 * Reads/writes the config at an arbitrary path. The daemon runs as root, so
 * its files are usually unreadable by an app: try direct file access first,
 * then fall back to `su`.
 */
object ConfigStore {
    fun read(path: String): String = try {
        File(path).readText()
    } catch (_: IOException) {
        su(path, "cat '$path'")
    }

    fun write(path: String, text: String) {
        try {
            File(path).writeText(text)
        } catch (_: IOException) {
            su(path, "cat > '$path'", text)
        }
    }

    private fun su(path: String, cmd: String, stdin: String? = null): String {
        require('\'' !in path) { "Invalid path" }
        val p = try {
            ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
        } catch (e: IOException) {
            throw IOException("Cannot access $path (no root available)")
        }
        p.outputStream.use { if (stdin != null) it.write(stdin.toByteArray()) }
        val out = p.inputStream.bufferedReader().readText()
        if (p.waitFor() != 0) throw IOException(out.trim().ifEmpty { "su failed for $path" })
        return out
    }
}
