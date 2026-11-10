package com.openhear.common.device

import com.juul.kable.*
import com.openhear.common.di.AppContainer
import com.openhear.common.getSystemBatteryPercentage
import com.openhear.common.protocol.FrameCodec
import com.openhear.common.protocol.ProtocolFrame
import com.openhear.common.protocol.TotaCrypto
import com.openhear.common.transport.RfcommTransport
import com.openhear.common.transport.createRfcommTransport
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Airdopes800Hearable(
    override val deviceName: String = "Airdopes 800 HiDef"
) : BaseHearable {

    companion object {
        const val RFCOMM_CHANNEL = 13 // Primary Bestechnic TOTA service channel
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var cachedAdvertisement: Advertisement? = null
    private var rfcommTransport: RfcommTransport? = null
    private var sessionAesKey: ByteArray? = null
    private var isSimulated: Boolean = false
    val isSimulatedConnection: Boolean
        get() = isSimulated
    private var userInitiatedDisconnect: Boolean = false
    private var reconnectJob: Job? = null
    private var observerJob: Job? = null
    private var initialQueryJob: Job? = null
    private var pollingJob: Job? = null

    private val _batteryState = MutableStateFlow(BatteryData(leftPercent = 100, rightPercent = 100, casePercent = 100))
    override val batteryState: StateFlow<BatteryData> = _batteryState.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionStatus> = _connectionState.asStateFlow()

    override val isBeastModeGestureControlled: Boolean = true

    private val _isLowLatencyEnabled = MutableStateFlow(false)
    override val isLowLatencyEnabled: StateFlow<Boolean> = _isLowLatencyEnabled.asStateFlow()

    @Volatile
    private var isLowLatencyQueryPending = false

    private val _currentBands = MutableStateFlow(listOf(0, 0, 0, 0, 0, 0, 0))
    val currentBands: StateFlow<List<Int>> = _currentBands.asStateFlow()

    override suspend fun connect(macAddress: String) {
        userInitiatedDisconnect = false
        reconnectJob?.cancel()
        observerJob?.cancel()
        initialQueryJob?.cancel()
        pollingJob?.cancel()

        Napier.i("Connecting to $deviceName [$macAddress]")
        _connectionState.value = ConnectionStatus.CONNECTING

        val sysBattery = getSystemBatteryPercentage(macAddress)
        if (sysBattery != null) {
            Napier.i("System reported battery level: $sysBattery%")
            _batteryState.value = BatteryData(leftPercent = sysBattery, rightPercent = sysBattery, casePercent = 100)
        }

        val transport = createRfcommTransport()
        rfcommTransport = transport

        val connectResult = transport.connect(macAddress, channel = RFCOMM_CHANNEL)
        if (connectResult.isSuccess) {
            Napier.i("RFCOMM transport connected. Initiating TOTA Cryptographic Handshake...")
            val handshakeSuccess = performTotaHandshake(transport)
            if (handshakeSuccess) {
                Napier.i("TOTA Cryptographic Handshake successful! Encrypted channel established.")
                isSimulated = false
                _connectionState.value = ConnectionStatus.CONNECTED

                // Observe decrypted frames from hardware
                observerJob = scope.launch {
                    val streamBuffer = ArrayList<Byte>()
                    try {
                        transport.observeIncoming().collect { rawData ->
                            for (b in rawData) streamBuffer.add(b)
                            val (frames, consumed) = TotaCrypto.unwrapTotaFramesWithConsumed(streamBuffer.toByteArray(), sessionAesKey)
                            if (consumed > 0 && consumed <= streamBuffer.size) {
                                val remainder = streamBuffer.subList(consumed, streamBuffer.size).toList()
                                streamBuffer.clear()
                                streamBuffer.addAll(remainder)
                            }
                            for (frameBytes in frames) {
                                handleIncomingNotification(frameBytes)
                            }
                        }
                    } catch (e: Exception) {
                        Napier.w("Incoming frame observer error: ${e.message}")
                    }
                }

                // Query initial status from physical earbuds
                initialQueryJob = scope.launch {
                    delay(300)
                    sendFrame(FrameCodec.encodeBatteryQuery(0x01)) // Left
                    delay(200)
                    sendFrame(FrameCodec.encodeBatteryQuery(0x02)) // Right
                    delay(200)
                    sendFrame(FrameCodec.encodeBatteryQuery(0x03)) // Case
                    delay(200)
                    isLowLatencyQueryPending = true
                    sendFrame(FrameCodec.encodeLowLatencyQuery())
                    delay(200)
                    sendFrame(FrameCodec.encodeEqualizerQuery())
                }

                // Periodic status polling:
                // Poll low latency state every 3s so physical touch toggles reflect promptly
                // Poll battery telemetry every 30s (10 cycles * 3s)
                pollingJob = scope.launch {
                    var cycleCount = 0
                    while (isActive && _connectionState.value == ConnectionStatus.CONNECTED) {
                        delay(3_000)
                        if (_connectionState.value == ConnectionStatus.CONNECTED && !isSimulated) {
                            isLowLatencyQueryPending = true
                            sendFrame(FrameCodec.encodeLowLatencyQuery())
                            cycleCount++
                            if (cycleCount >= 10) {
                                cycleCount = 0
                                delay(200)
                                sendFrame(FrameCodec.encodeBatteryQuery(0x01))
                                delay(200)
                                sendFrame(FrameCodec.encodeBatteryQuery(0x02))
                                delay(200)
                                sendFrame(FrameCodec.encodeBatteryQuery(0x03))
                            }
                        }
                    }
                }
                return
            } else {
                Napier.w("Handshake was not acknowledged by device.")
            }
        }

        Napier.w("RFCOMM could not connect or handshake failed with hardware at $macAddress.")
        isSimulated = false
        _connectionState.value = ConnectionStatus.DISCONNECTED
    }

    private suspend fun performTotaHandshake(transport: RfcommTransport): Boolean = withTimeoutOrNull(5000) {
        try {
            val incoming = transport.observeIncoming()
            val serverHelloDeferred = CompletableDeferred<ByteArray>()
            val serverSuccessDeferred = CompletableDeferred<Boolean>()

            val handshakeJob = scope.launch {
                val accumulator = ArrayList<Byte>()
                incoming.collect { chunk ->
                    for (b in chunk) accumulator.add(b)
                    val currentBytes = accumulator.toByteArray()

                    if (!serverHelloDeferred.isCompleted) {
                        val (frames, consumed) = TotaCrypto.unwrapTotaFramesWithConsumed(currentBytes, null)
                        val srvHello = frames.firstOrNull { it.size >= 5 && it[0] == 0x02.toByte() && it[1] == 0x10.toByte() }
                        if (srvHello != null) {
                            val sRand = TotaCrypto.parseServerHello(srvHello)
                            if (sRand != null) {
                                if (consumed > 0 && consumed <= accumulator.size) {
                                    val remainder = accumulator.subList(consumed, accumulator.size).toList()
                                    accumulator.clear()
                                    accumulator.addAll(remainder)
                                }
                                serverHelloDeferred.complete(sRand)
                            }
                        }
                    } else if (!serverSuccessDeferred.isCompleted) {
                        val (frames, consumed) = TotaCrypto.unwrapTotaFramesWithConsumed(currentBytes, null)
                        if (frames.any { TotaCrypto.isServerSuccess(it) }) {
                            if (consumed > 0 && consumed <= accumulator.size) {
                                val remainder = accumulator.subList(consumed, accumulator.size).toList()
                                accumulator.clear()
                                accumulator.addAll(remainder)
                            }
                            serverSuccessDeferred.complete(true)
                        }
                    }
                }
            }

            // Step 1: Send Client Hello
            val clientRand = ByteArray(32) { (it * 7 xor 0x5A).toByte() }
            val helloPkt = TotaCrypto.encodeClientHello(clientRand)
            transport.send(helloPkt)

            // Step 2: Await Server Hello
            val sRand = serverHelloDeferred.await()

            // Step 3: Derive Session Key & Send Client Verify
            val (aesKey, hash) = TotaCrypto.deriveSessionKey(sRand, clientRand)
            sessionAesKey = aesKey

            val verifyPkt = TotaCrypto.encodeClientVerify(hash)
            transport.send(verifyPkt)

            // Step 4: Verify Server Success response
            val success = serverSuccessDeferred.await()
            handshakeJob.cancel()
            success
        } catch (e: Exception) {
            Napier.e("Handshake failed: ${e.message}", e)
            false
        }
    } ?: false

    override suspend fun disconnect() {
        userInitiatedDisconnect = true
        reconnectJob?.cancel()
        reconnectJob = null
        observerJob?.cancel()
        observerJob = null
        initialQueryJob?.cancel()
        initialQueryJob = null
        pollingJob?.cancel()
        pollingJob = null

        Napier.i("Disconnecting from $deviceName")
        try {
            rfcommTransport?.disconnect()
        } catch (e: Exception) {
            Napier.w("Error during disconnect: ${e.message}")
        } finally {
            rfcommTransport = null
            sessionAesKey = null
            isSimulated = false
            _connectionState.value = ConnectionStatus.DISCONNECTED
        }
    }

    override suspend fun setLowLatencyMode(enabled: Boolean): Result<Unit> {
        Napier.i("Setting Low Latency (Beast Mode) = $enabled")
        if (isSimulated) {
            _isLowLatencyEnabled.value = enabled
            return Result.success(Unit)
        }

        val frame = FrameCodec.encodeLowLatency(enabled)
        val result = sendFrame(frame)
        if (result.isSuccess) {
            _isLowLatencyEnabled.value = enabled
        }
        return result
    }

    override suspend fun queryLowLatency(): Result<Unit> {
        if (isSimulated) return Result.success(Unit)
        return sendFrame(FrameCodec.encodeLowLatencyQuery())
    }

    override suspend fun setEqualizer(bands: List<Int>): Result<Unit> {
        Napier.i("Setting Equalizer bands: $bands")
        if (isSimulated) {
            _currentBands.value = bands
            return Result.success(Unit)
        }

        val frame = FrameCodec.encodeEqualizer(bands)
        val result = sendFrame(frame)
        if (result.isSuccess) {
            _currentBands.value = bands
        }
        return result
    }

    override suspend fun remapGesture(gesture: GestureType, action: ActionType, isRightEarbud: Boolean): Result<Unit> {
        Napier.i("Remapping gesture $gesture to action $action (isRightEarbud=$isRightEarbud)")
        if (isSimulated) {
            return Result.success(Unit)
        }

        val frame = FrameCodec.encodeGestureRemap(gesture, action, isRightEarbud)
        return sendFrame(frame)
    }

    private suspend fun sendFrame(frameBytes: ByteArray): Result<Unit> = withContext(Dispatchers.Default) {
        if (isSimulated) {
            return@withContext Result.success(Unit)
        }

        val transport = rfcommTransport
        val key = sessionAesKey
        if (transport == null || !transport.isConnected || key == null) {
            Napier.w("Cannot send frame: device not connected or session key missing")
            return@withContext Result.failure(IllegalStateException("Device not connected"))
        }

        val wrappedFrame = TotaCrypto.wrapTotaFrame(frameBytes, key)
        transport.send(wrappedFrame)
    }

    private fun handleIncomingNotification(data: ByteArray) {
        val hex = data.joinToString(" ") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
        Napier.i("RAW incoming frame [${data.size}B]: $hex")

        if (data.size == 4 && data[0] == FrameCodec.MAGIC_LSB && data[1] == FrameCodec.MAGIC_MSB) {
            Napier.d("Hardware ACK received: 0x08 0x63 0x00 0x00")
            return
        }

        val frame = FrameCodec.decodeResponseFrame(data) ?: FrameCodec.decodeFrame(data)
        if (frame == null) {
            Napier.d("Ignoring non-command payload (${data.size} bytes)")
            return
        }

        when (frame.opcode) {
            FrameCodec.OP_ACK -> {
                val statusByte = frame.payload.firstOrNull() ?: 0
                if (isLowLatencyQueryPending) {
                    isLowLatencyQueryPending = false
                    val enabled = (statusByte == 1.toByte())
                    Napier.i("Low Latency query response resolved: status=$statusByte -> isEnabled=$enabled")
                    _isLowLatencyEnabled.value = enabled
                } else {
                    Napier.d("Hardware ACK received: status=$statusByte")
                }
            }
            FrameCodec.OP_BATTERY -> {
                val battery = FrameCodec.parseBattery(frame.payload, _batteryState.value)
                Napier.i("Battery updated: L=${battery.leftPercent}%, R=${battery.rightPercent}%, Case=${battery.casePercent}%")
                _batteryState.value = battery
            }
            FrameCodec.OP_BEAST_MODE -> {
                isLowLatencyQueryPending = false
                val hexStr = frame.payload.joinToString(" ") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
                val enabled = FrameCodec.parseLowLatency(frame.payload)
                Napier.i("Low Latency (Beast Mode) state notification: payload=[$hexStr] -> parsed=$enabled")
                if (enabled != null) {
                    _isLowLatencyEnabled.value = enabled
                }
            }
            FrameCodec.OP_EQ -> {
                val bands = FrameCodec.parseEqualizer(frame.payload)
                if (bands.isNotEmpty()) {
                    Napier.i("EQ state notification: $bands")
                    _currentBands.value = bands
                }
            }
            FrameCodec.OP_GESTURE -> {
                val hexStr = frame.payload.joinToString(" ") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
                Napier.i("Gesture packet from hardware: payload=[$hexStr]")
            }
            else -> {
                val hexStr = frame.payload.joinToString(" ") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
                val opHex = (frame.opcode.toInt() and 0xFF).toString(16).padStart(2, '0')
                Napier.i("Unhandled frame opcode 0x$opHex, payload=[$hexStr]")
            }
        }
    }
}
