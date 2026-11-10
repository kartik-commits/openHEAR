package com.openhear.common.device

import kotlinx.coroutines.flow.StateFlow

data class BatteryData(
    val leftPercent: Int? = null,
    val rightPercent: Int? = null,
    val casePercent: Int? = null
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class GestureType {
    SINGLE_TAP,
    DOUBLE_TAP,
    LONG_PRESS
}

enum class ActionType {
    PLAY_PAUSE,
    NEXT_TRACK,
    PREVIOUS_TRACK,
    TOGGLE_LOW_LATENCY,
    VOLUME_UP,
    VOLUME_DOWN,
    VOICE_ASSISTANT,
    NONE
}

interface BaseHearable {
    val deviceName: String
    val batteryState: StateFlow<BatteryData>
    val connectionState: StateFlow<ConnectionStatus>

    val isBeastModeGestureControlled: Boolean get() = false
    val isLowLatencyEnabled: StateFlow<Boolean> get() = kotlinx.coroutines.flow.MutableStateFlow(false)

    suspend fun connect(macAddress: String)
    suspend fun disconnect()

    suspend fun queryLowLatency(): Result<Unit> = Result.success(Unit)
    suspend fun setLowLatencyMode(enabled: Boolean): Result<Unit>
    suspend fun setEqualizer(bands: List<Int>): Result<Unit>
    suspend fun remapGesture(gesture: GestureType, action: ActionType, isRightEarbud: Boolean = false): Result<Unit>
}
