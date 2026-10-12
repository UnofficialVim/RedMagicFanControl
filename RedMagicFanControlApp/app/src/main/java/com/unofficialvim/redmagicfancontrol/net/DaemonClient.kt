package com.unofficialvim.redmagicfancontrol.net

import android.net.LocalSocket
import android.net.LocalSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.OutputStream

/**
 * Line protocol of redmagicfancontrold: one command per write, one line back.
 * The daemon reads a single 256-byte buffer per wakeup, so commands are
 * strictly serialized (request, then reply) - never pipelined.
 *
 * The daemon runs as root and its socket sits in a root-only directory, so an
 * ordinary app cannot connect (file permissions + SELinux). We try a direct
 * connection first and otherwise bridge through `su -c nc -U <path>`, whose
 * stdin/stdout are the socket.
 */
class DaemonClient(private val path: String) {
    private class Conn(val out: OutputStream, val input: BufferedReader, val closer: Closeable)

    private val lock = Mutex()
    private var conn: Conn? = null

    suspend fun request(command: String): String = lock.withLock {
        coroutineScope {
            // Blocking reads can't be cancelled; closing the connection unblocks them.
            val watchdog = launch { delay(TIMEOUT_MS); close() }
            try {
                withContext(Dispatchers.IO) {
                    val c = conn ?: connect().also { conn = it }
                    c.out.write("$command\n".toByteArray())
                    c.out.flush()
                    c.input.readLine() ?: throw IOException("Daemon closed the connection")
                }
            } catch (e: IOException) {
                close()
                throw e
            } finally {
                watchdog.cancel()
            }
        }
    }

    private fun connect(): Conn {
        val direct = try {
            val s = LocalSocket()
            try {
                s.connect(LocalSocketAddress(path, LocalSocketAddress.Namespace.FILESYSTEM))
            } catch (e: IOException) {
                s.close(); throw e
            }
            Conn(s.outputStream, s.inputStream.bufferedReader(), s)
        } catch (e: IOException) {
            e
        }
        if (direct is Conn) return direct
        return viaSu(direct as IOException)
    }

    private fun viaSu(directError: IOException): Conn {
        require('\'' !in path) { "Invalid socket path" }
        val p = try {
            ProcessBuilder("su", "-c", "nc -U '$path'").redirectErrorStream(true).start()
        } catch (e: IOException) {
            throw IOException("Socket not reachable (${directError.message}) and no root available")
        }
        // nc exits right away if it can't connect; give it a moment to fail.
        Thread.sleep(200)
        if (!p.isAlive) {
            val msg = p.inputStream.bufferedReader().readText().trim()
            throw IOException(msg.ifEmpty { "nc exited with ${p.exitValue()}" })
        }
        return Conn(p.outputStream, p.inputStream.bufferedReader(), Closeable { p.destroy() })
    }

    fun close() {
        runCatching { conn?.closer?.close() }
        conn = null
    }

    private companion object {
        const val TIMEOUT_MS = 3000L
    }
}
