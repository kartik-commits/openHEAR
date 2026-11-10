package com.openhear.common.protocol

import com.openhear.common.device.ActionType
import com.openhear.common.device.BatteryData
import com.openhear.common.device.GestureType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FrameCodecTest {

    @Test
    fun testMagicBytesHeader() {
        val frame = FrameCodec.encodeBatteryQuery()
        assertEquals(5, frame.size)
        assertEquals(FrameCodec.MAGIC_LSB, frame[0])
        assertEquals(FrameCodec.MAGIC_MSB, frame[1])
        assertEquals(1, frame[2]) // len LSB
        assertEquals(0, frame[3]) // len MSB
        assertEquals(FrameCodec.OP_BATTERY, frame[4])
    }

    @Test
    fun testLowLatencyEncodeDecode() {
        val encodedTrue = FrameCodec.encodeLowLatency(true)
        val frameTrue = FrameCodec.decodeFrame(encodedTrue)
        assertNotNull(frameTrue)
        assertEquals(FrameCodec.OP_BEAST_MODE, frameTrue.opcode)
        assertEquals(1, frameTrue.payload.size)
        assertEquals(0x01.toByte(), frameTrue.payload[0])
        assertEquals(true, FrameCodec.parseLowLatency(frameTrue.payload))

        val encodedFalse = FrameCodec.encodeLowLatency(false)
        val frameFalse = FrameCodec.decodeFrame(encodedFalse)
        assertNotNull(frameFalse)
        assertEquals(FrameCodec.OP_BEAST_MODE, frameFalse.opcode)
        assertEquals(1, frameFalse.payload.size)
        assertEquals(0x00.toByte(), frameFalse.payload[0])
        assertEquals(false, FrameCodec.parseLowLatency(frameFalse.payload))
    }

    @Test
    fun testLowLatencyQueryEncode() {
        val query = FrameCodec.encodeLowLatencyQuery()
        val frame = FrameCodec.decodeFrame(query)
        assertNotNull(frame)
        assertEquals(FrameCodec.OP_BEAST_MODE, frame.opcode)
        assertEquals(1, frame.payload.size)
        assertEquals(0xFF.toByte(), frame.payload[0])
    }

    @Test
    fun testEqualizerPresetAndCustomEncodeDecode() {
        val encodedPreset = FrameCodec.encodeEqualizerPreset(FrameCodec.EQ_PRESET_BALANCED)
        val framePreset = FrameCodec.decodeFrame(encodedPreset)
        assertNotNull(framePreset)
        assertEquals(FrameCodec.OP_EQ, framePreset.opcode)
        assertEquals(2, framePreset.payload.size)
        assertEquals(0x03.toByte(), framePreset.payload[0])
        assertEquals(FrameCodec.EQ_PRESET_BALANCED, framePreset.payload[1])

        val customBands = listOf(2, 4, -1, 3, 0)
        val encodedCustom = FrameCodec.encodeEqualizer(customBands)
        val frameCustom = FrameCodec.decodeFrame(encodedCustom)
        assertNotNull(frameCustom)
        assertEquals(FrameCodec.OP_EQ, frameCustom.opcode)
        assertEquals(6, frameCustom.payload.size)
        assertEquals(customBands, FrameCodec.parseEqualizer(frameCustom.payload))
    }

    @Test
    fun testGestureRemapEncode() {
        val encoded = FrameCodec.encodeGestureRemap(
            gesture = GestureType.DOUBLE_TAP,
            action = ActionType.TOGGLE_LOW_LATENCY,
            isRightEarbud = true
        )
        val frame = FrameCodec.decodeFrame(encoded)
        assertNotNull(frame)
        assertEquals(FrameCodec.OP_GESTURE, frame.opcode)
        assertEquals(3, frame.payload.size)
        assertEquals(0x02.toByte(), frame.payload[0]) // Right earbud (2)
        assertEquals(0x02.toByte(), frame.payload[1]) // Double tap (2)
        assertEquals(0x0B.toByte(), frame.payload[2]) // Beast Mode (11 / 0x0B)
    }

    @Test
    fun testBatteryResponseParsing() {
        var battery = BatteryData(leftPercent = 50, rightPercent = 50, casePercent = 50)
        // Left earbud update
        battery = FrameCodec.parseBattery(byteArrayOf(0x01, 85.toByte()), battery)
        assertEquals(85, battery.leftPercent)
        assertEquals(50, battery.rightPercent)

        // Right earbud update
        battery = FrameCodec.parseBattery(byteArrayOf(0x02, 90.toByte()), battery)
        assertEquals(85, battery.leftPercent)
        assertEquals(90, battery.rightPercent)

        // Case update
        battery = FrameCodec.parseBattery(byteArrayOf(0x03, 100.toByte()), battery)
        assertEquals(100, battery.casePercent)
    }

    @Test
    fun testCorruptedFrameRejection() {
        val valid = FrameCodec.encodeLowLatency(true)

        // Invalid magic header
        val badMagic = valid.copyOf().also { it[0] = 0x00 }
        assertNull(FrameCodec.decodeFrame(badMagic))

        // Truncated length
        val truncated = valid.copyOfRange(0, valid.size - 2)
        assertNull(FrameCodec.decodeFrame(truncated))
    }

    @Test
    fun testDecodeEarbudResponseFrame() {
        // Earbud response packet: MAGIC (2B) + LEN (2B = 5B: SEQ 2B + OPCODE 1B + PAYLOAD 2B) + SEQ (2B) + OPCODE (0x04 Battery) + TARGET (0x01) + LEVEL (85)
        val earbudBatteryPacket = byteArrayOf(
            FrameCodec.MAGIC_LSB, FrameCodec.MAGIC_MSB,
            0x05, 0x00, // payload length = 5 (SEQ 2B + OPCODE 1B + TARGET 1B + LEVEL 1B)
            0x01, 0x00, // sequence number
            FrameCodec.OP_BATTERY,
            0x01, // Left earbud
            85.toByte() // 85%
        )

        val frame = FrameCodec.decodeResponseFrame(earbudBatteryPacket)
        assertNotNull(frame)
        assertEquals(FrameCodec.OP_BATTERY, frame.opcode)
        val battery = FrameCodec.parseBattery(frame.payload)
        assertEquals(85, battery.leftPercent)
    }

    @Test
    fun testDecodeEarbudEqNotification() {
        // Earbud EQ notification: MAGIC + LEN + SEQ + OP_EQ + PRESET + SUBCMD + 7 BANDS centered at 5
        val bands = byteArrayOf(5, 7, 5, 5, 3, 5, 5) // dB gains: [0, 2, 0, 0, -2, 0, 0]
        val earbudEqPacket = byteArrayOf(
            FrameCodec.MAGIC_LSB, FrameCodec.MAGIC_MSB,
            12.toByte(), 0x00, // len = 12
            0x05, 0x00, // seq = 5
            FrameCodec.OP_EQ,
            0x02, // preset balanced
            0x00 // subcmd
        ) + bands

        val frame = FrameCodec.decodeResponseFrame(earbudEqPacket)
        assertNotNull(frame)
        assertEquals(FrameCodec.OP_EQ, frame.opcode)
        val parsedBands = FrameCodec.parseEqualizer(frame.payload)
        assertEquals(listOf(0, 2, 0, 0, -2, 0, 0), parsedBands)
    }

    @Test
    fun testRealPhysicalHardwareFrames() {
        // Real hardware battery frame: 08 63 00 00 03 00 04 01 50 (Left earbud 80%)
        val hwBattery = byteArrayOf(
            0x08, 0x63, 0x00, 0x00, 0x03, 0x00, 0x04, 0x01, 0x50
        )
        val batFrame = FrameCodec.decodeResponseFrame(hwBattery)
        assertNotNull(batFrame)
        assertEquals(FrameCodec.OP_BATTERY, batFrame.opcode)
        val bat = FrameCodec.parseBattery(batFrame.payload)
        assertEquals(80, bat.leftPercent)

        // Real hardware EQ frame: 08 63 00 00 0a 00 13 02 ff 05 05 05 05 05 05 05
        val hwEq = byteArrayOf(
            0x08, 0x63, 0x00, 0x00, 0x0a, 0x00, 0x13, 0x02, 0xff.toByte(),
            0x05, 0x05, 0x05, 0x05, 0x05, 0x05, 0x05
        )
        val eqFrame = FrameCodec.decodeResponseFrame(hwEq)
        assertNotNull(eqFrame)
        assertEquals(FrameCodec.OP_EQ, eqFrame.opcode)
        val eqBands = FrameCodec.parseEqualizer(eqFrame.payload)
        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0), eqBands)
    }

    @Test
    fun testRealHardwareBeastModeStatusResponses() {
        // Real hardware generic ACK frame: 08 63 00 00 01 00 00
        val hwAck = byteArrayOf(0x08, 0x63, 0x00, 0x00, 0x01, 0x00, 0x00)
        val frameAck = FrameCodec.decodeResponseFrame(hwAck)
        assertNotNull(frameAck)
        assertEquals(FrameCodec.OP_ACK, frameAck.opcode)

        // Real hardware Beast Mode notification with opcode 0x81 when OFF: 08 63 00 00 02 00 81 00
        val hwBeastOff = byteArrayOf(0x08, 0x63, 0x00, 0x00, 0x02, 0x00, 0x81.toByte(), 0x00)
        val frameOff = FrameCodec.decodeResponseFrame(hwBeastOff)
        assertNotNull(frameOff)
        assertEquals(FrameCodec.OP_BEAST_MODE, frameOff.opcode)
        assertEquals(false, FrameCodec.parseLowLatency(frameOff.payload))

        // Real hardware Beast Mode notification with opcode 0x81 when ON: 08 63 00 00 02 00 81 01
        val hwBeastOn = byteArrayOf(0x08, 0x63, 0x00, 0x00, 0x02, 0x00, 0x81.toByte(), 0x01)
        val frameOn = FrameCodec.decodeResponseFrame(hwBeastOn)
        assertNotNull(frameOn)
        assertEquals(FrameCodec.OP_BEAST_MODE, frameOn.opcode)
        assertEquals(true, FrameCodec.parseLowLatency(frameOn.payload))
    }
}
