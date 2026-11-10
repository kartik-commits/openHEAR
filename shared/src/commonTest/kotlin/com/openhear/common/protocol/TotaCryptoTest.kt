package com.openhear.common.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TotaCryptoTest {

    @Test
    fun testCrc32StandardVector() {
        val testData = "123456789".encodeToByteArray()
        val crc = Crc32.compute(testData)
        assertEquals(0xCBF43926L, crc)
    }

    @Test
    fun testEnvelopeWrapAndUnwrapPlain() {
        val payload = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
        val wrapped = TotaCrypto.wrapTotaFrame(payload)
        val unwrapped = TotaCrypto.unwrapTotaFrames(wrapped)

        assertEquals(1, unwrapped.size)
        assertTrue(payload.contentEquals(unwrapped[0]))
    }

    @Test
    fun testEnvelopeWrapAndUnwrapEncrypted() {
        val aesKey = ByteArray(16) { it.toByte() }
        val payload = FrameCodec.encodeBatteryQuery()

        val wrapped = TotaCrypto.wrapTotaFrame(payload, aesKey)
        val unwrapped = TotaCrypto.unwrapTotaFrames(wrapped, aesKey)

        assertEquals(1, unwrapped.size)
        assertTrue(payload.contentEquals(unwrapped[0]))
    }

    @Test
    fun testRecordedServerHelloParse() {
        // Real Server Hello received from physical Airdopes 800 hardware
        val serverHelloFrame = byteArrayOf(
            0x02, 0x10, 0x05, 0x00, 0x04,
            0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte()
        )
        val parsed = TotaCrypto.parseServerHello(serverHelloFrame)
        assertNotNull(parsed)
        assertEquals(4, parsed.size)
        assertEquals(0xAA.toByte(), parsed[0])
        assertEquals(0xDD.toByte(), parsed[3])
    }

    @Test
    fun testServerSuccessVerification() {
        val successPayload = "connect success.".encodeToByteArray()
        assertTrue(TotaCrypto.isServerSuccess(successPayload))
    }

    @Test
    fun testKeyDerivationDeterministic() {
        val srvRand = ByteArray(32) { (it * 3).toByte() }
        val clientRand = ByteArray(32) { (it * 7).toByte() }

        val (key1, hash1) = TotaCrypto.deriveSessionKey(srvRand, clientRand)
        val (key2, hash2) = TotaCrypto.deriveSessionKey(srvRand, clientRand)

        assertEquals(16, key1.size)
        assertEquals(32, hash1.size)
        assertTrue(key1.contentEquals(key2))
        assertTrue(hash1.contentEquals(hash2))
    }
}
