package com.openhear.common.device

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.aakira.napier.Napier

/**
 * Singleton-like connection manager to prevent duplicate GATT/RFCOMM connections.
 * It observes the underlying StateFlows of the active BaseHearable device.
 */
class DeviceConnectionManager {

    // Only one active device is handled at a time to prevent conflicts
    private var activeDevice: BaseHearable? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var observeJob: Job? = null

    private val _globalConnectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val globalConnectionState: StateFlow<ConnectionStatus> = _globalConnectionState.asStateFlow()

    private val _globalBatteryState = MutableStateFlow(BatteryData())
    val globalBatteryState: StateFlow<BatteryData> = _globalBatteryState.asStateFlow()

    private val _globalLowLatencyState = MutableStateFlow(false)
    val globalLowLatencyState: StateFlow<Boolean> = _globalLowLatencyState.asStateFlow()

    suspend fun connectToDevice(device: BaseHearable, macAddress: String) {
        if (activeDevice != null) {
            Napier.w("Disconnecting previously active device before new connection.")
            disconnectCurrent()
        }

        Napier.i("Initiating connection to ${device.deviceName} at $macAddress")
        activeDevice = device

        observeJob?.cancel()
        observeJob = scope.launch {
            launch {
                device.connectionState.collect { state ->
                    _globalConnectionState.value = state
                }
            }
            launch {
                device.batteryState.collect { battery ->
                    _globalBatteryState.value = battery
                }
            }
            launch {
                device.isLowLatencyEnabled.collect { isEnabled ->
                    _globalLowLatencyState.value = isEnabled
                }
            }
        }

        try {
            _globalConnectionState.value = ConnectionStatus.CONNECTING
            device.connect(macAddress)
        } catch (e: Exception) {
            Napier.e("Failed to connect", e)
            _globalConnectionState.value = ConnectionStatus.ERROR
        }
    }

    suspend fun disconnectCurrent() {
        observeJob?.cancel()
        observeJob = null
        activeDevice?.disconnect()
        activeDevice = null
        _globalConnectionState.value = ConnectionStatus.DISCONNECTED
        _globalBatteryState.value = BatteryData()
        _globalLowLatencyState.value = false
    }

    suspend fun queryLowLatency(): Result<Unit> {
        return activeDevice?.queryLowLatency() ?: Result.success(Unit)
    }

    fun getActiveDevice(): BaseHearable? = activeDevice
}
