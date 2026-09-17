package com.unofficialvim.rmfcapp.net

import android.content.Context
import android.net.LocalSocket
import android.net.LocalSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Stub client for talking to a native daemon over a Unix domain socket.
 *
 * This intentionally does not implement a wire protocol yet: connect()
 * opens the socket, and sendCommand() is a placeholder round-trip. Fill
 * in the actual framing/format (e.g. line-delimited JSON dispatched to a
 * command handler on the daemon side) once that's decided.
 *
 * Uses FILESYSTEM-namespace sockets (a real path under the app's
 * filesDir) rather than the abstract namespace, so the socket file's
 * own permissions can gate access.
 */
class DomainSocketClient(
    private val socketName: String,
    private val namespace: LocalSocketAddress.Namespace = LocalSocketAddress.Namespace.FILESYSTEM
) {
    private var socket: LocalSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    val isConnected: Boolean
        get() = socket?.isConnected == true

    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val newSocket = LocalSocket()
            newSocket.connect(LocalSocketAddress(socketName, namespace))
            socket = newSocket
            input = newSocket.inputStream
            output = newSocket.outputStream
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Stub round-trip: writes the command and reports success, but does
     * not yet read/parse a real response. Replace the body once the
     * daemon's command dispatcher protocol is finalized.
     */
    suspend fun sendCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        val out = output
        if (out == null || !isConnected) {
            return@withContext Result.failure(IllegalStateException("Not connected to daemon"))
        }
        try {
            out.write((command + "\n").toByteArray(Charsets.UTF_8))
            out.flush()
            // TODO: read and parse the daemon's actual response here.
            Result.success("stub-response")
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    fun disconnect() {
        try {
            input?.close()
            output?.close()
            socket?.close()
        } catch (_: IOException) {
            // Already closed or never opened - nothing to do.
        } finally {
            input = null
            output = null
            socket = null
        }
    }

    companion object {
        /** Default socket location: a file under the app's private files dir. */
        fun defaultSocketPath(context: Context, fileName: String = "daemon.sock"): String =
            File(context.filesDir, fileName).absolutePath
    }
}
