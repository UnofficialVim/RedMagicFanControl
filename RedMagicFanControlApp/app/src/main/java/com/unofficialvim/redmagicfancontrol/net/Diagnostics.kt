package com.unofficialvim.redmagicfancontrol.net

import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.unofficialvim.redmagicfancontrol.data.ConfigStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Step-by-step connectivity check; returns a plain-text log the user can paste back. */
object Diagnostics {
    suspend fun run(socketPath: String, configPath: String): String = withContext(Dispatchers.IO) {
        val log = StringBuilder()
        fun step(name: String, block: () -> String) {
            val r = try { block() } catch (e: Throwable) { "FAIL: ${e.javaClass.simpleName}: ${e.message}" }
            log.append("[$name]\n$r\n\n")
        }

        step("1 socket file (app view)") {
            val f = File(socketPath)
            "exists=${f.exists()} canRead=${f.canRead()} canWrite=${f.canWrite()}"
        }
        step("2 direct connect") {
            LocalSocket().use { s ->
                s.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))
                "OK (connected without root)"
            }
        }
        step("3 su available") { sh("su -c id") }
        step("4 socket file (as root)") { sh("su -c \"ls -lZ '$socketPath'\"") }
        step("5 nc supports -U") { sh("su -c \"nc --help 2>&1 | head -3\"") }
        step("6 getCpuTemp via su+nc") {
            val p = ProcessBuilder("su", "-c", "nc -U '$socketPath'").redirectErrorStream(true).start()
            p.outputStream.apply { write("getCpuTemp\n".toByteArray()); flush() }
            val reader = p.inputStream.bufferedReader()
            val t = Thread.currentThread()
            val killer = Thread { Thread.sleep(3000); p.destroy() }.apply { start() }
            val line = try { reader.readLine() } finally { killer.interrupt(); p.destroy() }
            "reply=${line ?: "<none/timeout>"}"
        }
        step("7 config file") {
            val t = ConfigStore.read(configPath)
            "OK, ${t.length} bytes"
        }
        log.toString()
    }

    private fun sh(cmd: String): String {
        val p = ProcessBuilder("sh", "-c", cmd).redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().readText().trim()
        return "exit=${p.waitFor()} $out"
    }
}
