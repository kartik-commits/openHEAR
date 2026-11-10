package com.openhear.common.transport

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

actual fun createRfcommTransport(): RfcommTransport = AndroidRfcommTransport()

class AndroidRfcommTransport : RfcommTransport {

    companion object {
        val BES_UUID: UUID = UUID.fromString("66666666-6666-6666-6666-666666666666")
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var socket: BluetoothSocket? = null
    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var readJob: Job? = null

    private val _incomingFlow = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 64)

    override val isConnected: Boolean
        get() = socket?.isConnected == true

    override suspend fun connect(macAddress: String, channel: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect()

            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: return@withContext Result.failure(IllegalStateException("Bluetooth adapter not available"))

            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }

            val device = adapter.getRemoteDevice(macAddress)

            // Try connecting via BES vendor UUID, fallback to SPP UUID, fallback to reflection channel
            var lastException: Exception? = null
            var connectedSocket: BluetoothSocket? = null

            val attempts: List<() -> BluetoothSocket> = listOf(
                { device.createInsecureRfcommSocketToServiceRecord(BES_UUID) },
                { device.createInsecureRfcommSocketToServiceRecord(SPP_UUID) },
                {
                    val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    method.invoke(device, channel) as BluetoothSocket
                }
            )

            for (attempt in attempts) {
                try {
                    val s = attempt()
                    s.connect()
                    connectedSocket = s
                    break
                } catch (e: Exception) {
                    lastException = e
                    Napier.w("Socket attempt failed: ${e.message}")
                }
            }

            if (connectedSocket == null) {
                val err = lastException ?: IllegalStateException("Failed to establish RFCOMM connection to $macAddress")
                Napier.e("Android RFCOMM connection failed: ${err.message}", err)
                return@withContext Result.failure(err)
            }

            socket = connectedSocket
            inStream = connectedSocket.inputStream
            outStream = connectedSocket.outputStream

            startReadLoop()
            Napier.i("Android RFCOMM connected to $macAddress successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Android RFCOMM connection failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun startReadLoop() {
        val stream = inStream ?: return
        readJob?.cancel()
        readJob = scope.launch {
            val buffer = ByteArray(2048)
            while (isActive && isConnected) {
                try {
                    val bytesRead = stream.read(buffer)
                    if (bytesRead > 0) {
                        _incomingFlow.emit(buffer.copyOfRange(0, bytesRead))
                    } else if (bytesRead < 0) {
                        break
                    }
                } catch (e: Exception) {
                    Napier.w("Android RFCOMM read stopped: ${e.message}")
                    break
                }
            }
            disconnect()
        }
    }

    override suspend fun send(data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val stream = outStream
        if (stream == null || !isConnected) {
            return@withContext Result.failure(IllegalStateException("Device not connected"))
        }

        try {
            stream.write(data)
            stream.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("Android RFCOMM send error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun observeIncoming(): Flow<ByteArray> = _incomingFlow.asSharedFlow()

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        readJob?.cancel()
        readJob = null

        try {
            inStream?.close()
        } catch (_: Exception) {}
        try {
            outStream?.close()
        } catch (_: Exception) {}
        try {
            socket?.close()
        } catch (_: Exception) {}

        inStream = null
        outStream = null
        socket = null
    }
}
