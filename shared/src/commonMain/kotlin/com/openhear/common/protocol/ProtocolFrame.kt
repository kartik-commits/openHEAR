package com.openhear.common.protocol

import com.openhear.common.device.ActionType
import com.openhear.common.device.BatteryData
import com.openhear.common.device.GestureType

/**
 * Represents a parsed or pending Bluetooth packet frame for Bestechnic (BES TOTA).
 * Format:
 * [MAGIC_LSB (0x08)] [MAGIC_MSB (0x63)] [LEN_LSB (1B)] [LEN_MSB (1B)] [OPCODE (1B)] [PAYLOAD (NB)]
 */
data class ProtocolFrame(
    val opcode: Byte,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ProtocolFrame
        if (opcode != other.opcode) return false
        if (!payload.contentEquals(other.payload)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = opcode.toInt()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

object FrameCodec {
    const val MAGIC_LSB: Byte = 0x08
    const val MAGIC_MSB: Byte = 0x63 // 25352 (0x6308) in little-endian

    // BES TOTA Command Opcodes
    const val OP_ACK: Byte = 0x00
    const val OP_GESTURE: Byte = 0x01
    const val OP_BATTERY: Byte = 0x04
    const val OP_IN_EAR_DETECT: Byte = 0x07
    const val OP_EQ: Byte = 0x13
    const val OP_MULTIPOINT: Byte = 0x1A // Dual Device Connection
    const val OP_BEAST_MODE: Byte = 0x81.toByte() // 0x81 (-127) is Beast Mode (Low Latency Gaming Mode)

    // Presets for Airdopes 800
    const val EQ_PRESET_DEFAULT: Byte = 1
    const val EQ_PRESET_BALANCED: Byte = 2
    const val EQ_PRESET_ROCK: Byte = 3
    const val EQ_PRESET_CLASSIC: Byte = 4
    const val EQ_PRESET_POP: Byte = 5
    const val EQ_PRESET_CLUB: Byte = 6

    /**
     * Encloses opcode and payload into a BES TOTA frame.
     */
    fun encodeFrame(opcode: Byte, payload: ByteArray = byteArrayOf()): ByteArray {
        val totalPayloadLen = 1 + payload.size
        val buffer = ByteArray(4 + totalPayloadLen)

        buffer[0] = MAGIC_LSB
        buffer[1] = MAGIC_MSB
        buffer[2] = (totalPayloadLen and 0xFF).toByte()
        buffer[3] = ((totalPayloadLen shr 8) and 0xFF).toByte()
        buffer[4] = opcode

        if (payload.isNotEmpty()) {
            payload.copyInto(buffer, destinationOffset = 5)
        }

        return buffer
    }

    fun isKnownOpcode(op: Byte): Boolean {
        return op == OP_GESTURE ||
               op == OP_BATTERY ||
               op == OP_IN_EAR_DETECT ||
               op == OP_EQ ||
               op == OP_MULTIPOINT ||
               op == OP_BEAST_MODE
    }

    /**
     * Decodes an earbud response frame or unsolicited telemetry packet.
     * Earbud response frame layout (6 header bytes):
     * 08 63 (2B Magic), seq_lsb seq_msb (2B Sequence), len_lsb len_msb (2B Length), data bytes...
     *
     * Where len is the number of data bytes following index 5:
     * - If len >= 2: data[0] (byte 6) is opcode, data[1..len-1] (byte 7..5+len) is payload.
     * - If len == 1: data[0] (byte 6) is the single status byte.
     *   For Beast Mode / Low Latency query response, byte 6 is 0x00 (disabled) or 0x01 (enabled).
     */
    fun decodeResponseFrame(bytes: ByteArray): ProtocolFrame? {
        if (bytes.size < 6) return null
        if (bytes[0] != MAGIC_LSB || bytes[1] != MAGIC_MSB) return null

        val lenA = (bytes[4].toInt() and 0xFF) or ((bytes[5].toInt() and 0xFF) shl 8)
        val lenB = (bytes[2].toInt() and 0xFF) or ((bytes[3].toInt() and 0xFF) shl 8)

        // Real hardware layout: [08 63] [seq 2B = 00 00] [len 2B] [data...]
        // len is at bytes[4..5], data starts at byte 6
        val isRealHardwareFormat = (bytes[2] == 0x00.toByte() && bytes[3] == 0x00.toByte()) && (6 + lenA <= bytes.size && lenA > 0)
        if (isRealHardwareFormat) {
            val totalEnd = minOf(bytes.size, 6 + lenA)
            if (lenA >= 2 && bytes.size >= 7) {
                val opcode = bytes[6]
                val payload = if (totalEnd > 7) bytes.copyOfRange(7, totalEnd) else byteArrayOf()
                return ProtocolFrame(opcode = opcode, payload = payload)
            } else if (lenA == 1 && bytes.size >= 7) {
                val dataByte = bytes[6]
                return if (dataByte == 0x01.toByte()) {
                    // Beast Mode status response when active
                    ProtocolFrame(opcode = OP_BEAST_MODE, payload = byteArrayOf(0x01))
                } else {
                    // Generic TOTA hardware ACK / inactive status (0x00)
                    ProtocolFrame(opcode = OP_ACK, payload = byteArrayOf(dataByte))
                }
            }
        }

        // Alternative layout with [len 2B] at bytes[2..3], [seq 2B] at bytes[4..5], [opcode] at byte 6
        if (bytes.size >= 7 && (4 + lenB <= bytes.size && lenB > 0)) {
            val totalEnd = minOf(bytes.size, 4 + lenB)
            val opcode = bytes[6]
            val payload = if (totalEnd > 7) bytes.copyOfRange(7, totalEnd) else byteArrayOf()
            return ProtocolFrame(opcode = opcode, payload = payload)
        }

        return null
    }

    /**
     * Decodes a raw byte buffer into a [ProtocolFrame].
     * Host command frames place the opcode directly at index 4 with a 4-byte header.
     * Response frames are delegated to [decodeResponseFrame].
     */
    fun decodeFrame(bytes: ByteArray, isResponse: Boolean = false): ProtocolFrame? {
        if (isResponse) {
            return decodeResponseFrame(bytes)
        }
        if (bytes.size < 5) return null
        if (bytes[0] != MAGIC_LSB || bytes[1] != MAGIC_MSB) return null

        // If the frame has 6-byte header with sequence number at 2..3, delegate to response decoder
        if (bytes.size >= 7 && bytes[2] == 0x00.toByte() && bytes[3] == 0x00.toByte()) {
            val resp = decodeResponseFrame(bytes)
            if (resp != null) return resp
        }

        // Host command frame layout (4-byte header):
        // [08 63] [len_lsb, len_msb] [opcode] [payload...]
        val payloadLen = (bytes[2].toInt() and 0xFF) or ((bytes[3].toInt() and 0xFF) shl 8)
        val totalEnd = minOf(bytes.size, 4 + payloadLen)
        val opcode = bytes[4]
        val payload = if (totalEnd > 5) {
            bytes.copyOfRange(5, totalEnd)
        } else {
            byteArrayOf()
        }

        return ProtocolFrame(opcode = opcode, payload = payload)
    }

    // Encoding Helpers

    fun encodeBatteryQuery(target: Byte? = null): ByteArray {
        return if (target != null) {
            encodeFrame(OP_BATTERY, byteArrayOf(target))
        } else {
            encodeFrame(OP_BATTERY)
        }
    }

    fun encodeLowLatency(enabled: Boolean): ByteArray {
        val payload = byteArrayOf(if (enabled) 0x01 else 0x00)
        return encodeFrame(OP_BEAST_MODE, payload)
    }

    fun encodeLowLatencyQuery(): ByteArray {
        return encodeFrame(OP_BEAST_MODE, byteArrayOf(0xFF.toByte()))
    }

    fun encodeEqualizerPreset(preset: Byte): ByteArray {
        val payload = byteArrayOf(0x03, preset)
        return encodeFrame(OP_EQ, payload)
    }

    fun encodeEqualizerQuery(): ByteArray {
        return encodeFrame(OP_EQ, byteArrayOf(0x05))
    }

    fun encodeEqualizer(bands: List<Int>): ByteArray {
        // BES Custom EQ expects subcmd 0x04 followed by gains centered at +5
        val gains = ByteArray(bands.size) { i ->
            (bands[i] + 5).coerceIn(0, 10).toByte()
        }
        val payload = byteArrayOf(0x04) + gains
        return encodeFrame(OP_EQ, payload)
    }

    fun encodeGestureRemap(gesture: GestureType, action: ActionType, isRightEarbud: Boolean = false): ByteArray {
        val earbudByte = if (isRightEarbud) 0x02.toByte() else 0x01.toByte()
        val clickTypeByte = when (gesture) {
            GestureType.SINGLE_TAP -> 0x01.toByte()
            GestureType.DOUBLE_TAP -> 0x02.toByte()
            GestureType.LONG_PRESS -> 0x04.toByte()
        }
        // BES Airdopes gesture action codes verified from official boAt m70.java:
        // 0=Disable, 1=Previous, 2=Next, 3=ANC, 4=Callback, 5=Volume Up, 6=Volume Down,
        // 7=Play music, 8=Stop music, 9=Google Assistant, 10=Play/Pause, 11=Beast Mode
        val actionByte = when (action) {
            ActionType.NONE -> 0x00.toByte()
            ActionType.PREVIOUS_TRACK -> 0x01.toByte()
            ActionType.NEXT_TRACK -> 0x02.toByte()
            ActionType.VOLUME_UP -> 0x05.toByte()
            ActionType.VOLUME_DOWN -> 0x06.toByte()
            ActionType.VOICE_ASSISTANT -> 0x09.toByte()
            ActionType.PLAY_PAUSE -> 0x0A.toByte()
            ActionType.TOGGLE_LOW_LATENCY -> 0x0B.toByte()
        }
        val payload = byteArrayOf(earbudByte, clickTypeByte, actionByte)
        return encodeFrame(OP_GESTURE, payload)
    }

    // Decoding Helpers

    fun parseBattery(payload: ByteArray, current: BatteryData = BatteryData()): BatteryData {
        if (payload.isEmpty()) return current
        if (payload.size >= 2) {
            val target = payload[0].toInt() and 0xFF
            val percent = payload[1].toInt() and 0xFF
            return when (target) {
                1 -> current.copy(leftPercent = percent)
                2 -> current.copy(rightPercent = percent)
                3 -> current.copy(casePercent = percent)
                else -> current
            }
        }
        return current
    }

    fun parseLowLatency(payload: ByteArray): Boolean? {
        if (payload.isEmpty()) return null
        return when (payload[0]) {
            0x01.toByte() -> true
            0x00.toByte() -> false
            0xFF.toByte() -> {
                if (payload.size > 1) {
                    when (payload[1]) {
                        0x01.toByte() -> true
                        0x00.toByte() -> false
                        else -> null
                    }
                } else null
            }
            else -> null
        }
    }

    fun parseEqualizer(payload: ByteArray): List<Int> {
        if (payload.isEmpty()) return emptyList()
        // Band gains are shifted by +5 dB in BES TOTA format
        val startIndex = when {
            payload.size >= 9 -> 2 // Response format: preset byte + subcmd byte + 7 bands
            payload[0] == 0x04.toByte() -> 1 // Custom EQ command format: subcmd 0x04 + gains
            else -> 0
        }
        return payload.drop(startIndex).map { (it.toInt() and 0xFF) - 5 }
    }
}
