package com.openhear.common.protocol

/**
 * Bestechnic (BES) TOTA cryptographic protocol engine.
 * Handles key derivation, AES-128-CBC encryption/decryption,
 * and TOTA packet framing with CRC-32 integrity validation.
 */
object TotaCrypto {

    const val MAGIC_ENVELOPE: Byte = 0x9F.toByte()
    val HEAD = byteArrayOf(0x48, 0x45, 0x41, 0x44) // "HEAD"
    val TAIL = byteArrayOf(0x54, 0x41, 0x49, 0x4C) // "TAIL"
    val DEFAULT_IV = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

    fun packIntLe(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    fun unpackIntLe(bytes: ByteArray, offset: Int = 0): Int {
        return (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    fun packLongLe(value: Long): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    fun unpackLongLe(bytes: ByteArray, offset: Int = 0): Long {
        val b0 = bytes[offset].toLong() and 0xFF
        val b1 = bytes[offset + 1].toLong() and 0xFF
        val b2 = bytes[offset + 2].toLong() and 0xFF
        val b3 = bytes[offset + 3].toLong() and 0xFF
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    /**
     * Non-linear mixing function from Bestechnic protocol (ose.b).
     */
    fun mixBytes(b1: ByteArray, b2: ByteArray): ByteArray {
        val maxLen = maxOf(b1.size, b2.size)
        val a1 = ByteArray(maxLen)
        val a2 = ByteArray(maxLen)
        b1.copyInto(a1)
        b2.copyInto(a2)

        val out = ByteArray(maxLen)
        for (i in 0 until maxLen) {
            val byte1 = a1[i].toInt()
            val byte2 = a2[i].toInt()
            val res = when (i % 6) {
                0 -> byte1 + byte2
                1 -> byte1 * byte2
                2 -> byte1 and byte2
                3 -> byte1 or byte2
                4 -> byte1 xor byte2
                else -> (byte1 * byte1) + (byte2 * byte2)
            }
            out[i] = res.toByte()
        }
        return out
    }

    /**
     * Decimates 32-byte SHA-256 hash into 16-byte AES key (ose.a).
     */
    fun deriveAesKeyFromHash(hash32: ByteArray): ByteArray {
        val key = ByteArray(16)
        for (i in 0 until 16) {
            key[i] = hash32[i * 2]
        }
        return key
    }

    /**
     * Derives the session AES-128 key given server random bytes and client random bytes.
     */
    fun deriveSessionKey(serverRand: ByteArray, clientRand: ByteArray): Pair<ByteArray, ByteArray> {
        val mixed = mixBytes(serverRand, clientRand)
        val hash = sha256(mixed)
        val aesKey = deriveAesKeyFromHash(hash)
        return Pair(aesKey, hash)
    }

    /**
     * Wraps a payload into a TOTA v2 frame.
     * If [aesKey] is provided, payload is encrypted with AES-128-CBC.
     */
    fun wrapTotaFrame(data: ByteArray, aesKey: ByteArray? = null): ByteArray {
        val innerData = if (aesKey != null) {
            aesCbcEncrypt(aesKey, DEFAULT_IV, data)
        } else {
            data
        }

        val crc = Crc32.compute(innerData)
        val frameLen = 4 + 4 + innerData.size + 4 + 4
        val totaFrame = ByteArray(frameLen)

        // HEAD
        HEAD.copyInto(totaFrame, destinationOffset = 0)
        // Length of innerData
        packIntLe(innerData.size).copyInto(totaFrame, destinationOffset = 4)
        // innerData
        innerData.copyInto(totaFrame, destinationOffset = 8)
        // CRC-32
        packLongLe(crc).copyInto(totaFrame, destinationOffset = 8 + innerData.size)
        // TAIL
        TAIL.copyInto(totaFrame, destinationOffset = 8 + innerData.size + 4)

        // Outer envelope: [0x9F] [frameLen (4B)] [totaFrame]
        val outer = ByteArray(1 + 4 + totaFrame.size)
        outer[0] = MAGIC_ENVELOPE
        packIntLe(totaFrame.size).copyInto(outer, destinationOffset = 1)
        totaFrame.copyInto(outer, destinationOffset = 5)
        return outer
    }

    /**
     * Unwraps received bytes from TOTA stream into decrypted payload frames along with
     * the count of consumed bytes from the beginning of [stream].
     */
    fun unwrapTotaFramesWithConsumed(stream: ByteArray, aesKey: ByteArray? = null): Pair<List<ByteArray>, Int> {
        val results = mutableListOf<ByteArray>()
        var pos = 0
        while (pos < stream.size) {
            if (stream[pos] != MAGIC_ENVELOPE) {
                pos++
                continue
            }
            if (pos + 5 > stream.size) break
            val frameLen = unpackIntLe(stream, pos + 1)
            if (pos + 5 + frameLen > stream.size) break

            val frame = stream.copyOfRange(pos + 5, pos + 5 + frameLen)
            pos += 5 + frameLen

            if (frame.size < 16) continue
            // Verify HEAD
            if (!frame.copyOfRange(0, 4).contentEquals(HEAD)) continue
            // Verify TAIL
            if (!frame.copyOfRange(frame.size - 4, frame.size).contentEquals(TAIL)) continue

            val innerLen = unpackIntLe(frame, 4)
            if (innerLen < 0 || 8 + innerLen + 8 > frame.size) continue

            val innerData = frame.copyOfRange(8, 8 + innerLen)
            val expectedCrc = unpackLongLe(frame, 8 + innerLen)
            val computedCrc = Crc32.compute(innerData)
            if (computedCrc != expectedCrc) continue

            val decrypted = if (aesKey != null) {
                aesCbcDecrypt(aesKey, DEFAULT_IV, innerData)
            } else {
                innerData
            }

            // If the packet has a 4-byte transport prefix (e.g. 0x00, 0x60, len_lsb, len_msb)
            // before the BES TOTA magic header (0x08, 0x63), strip it.
            // Also extract the true packet length declared in the prefix (decrypted[2..3])
            // to strip AES-CBC zero padding cleanly.
            val clean = if (decrypted.size >= 6 && decrypted[4] == FrameCodec.MAGIC_LSB && decrypted[5] == FrameCodec.MAGIC_MSB) {
                val declaredLen = (decrypted[2].toInt() and 0xFF) or ((decrypted[3].toInt() and 0xFF) shl 8)
                val totalEnd = minOf(decrypted.size, 4 + declaredLen)
                decrypted.copyOfRange(4, totalEnd)
            } else if (decrypted.size >= 4 && decrypted[0] == FrameCodec.MAGIC_LSB && decrypted[1] == FrameCodec.MAGIC_MSB) {
                // Check if this is an earbud response frame: [08 63] [seq 2B = 00 00] [len 2B] ...
                val expectedTotalSize = if (decrypted.size >= 6 && decrypted[2] == 0x00.toByte() && decrypted[3] == 0x00.toByte()) {
                    val respLen = (decrypted[4].toInt() and 0xFF) or ((decrypted[5].toInt() and 0xFF) shl 8)
                    6 + respLen
                } else {
                    val totalPayloadLen = (decrypted[2].toInt() and 0xFF) or ((decrypted[3].toInt() and 0xFF) shl 8)
                    4 + totalPayloadLen
                }
                if (expectedTotalSize in 4..decrypted.size) {
                    decrypted.copyOfRange(0, expectedTotalSize)
                } else {
                    decrypted
                }
            } else {
                decrypted
            }

            results.add(clean)
        }
        return Pair(results, pos)
    }

    /**
     * Unwraps received bytes from TOTA stream into decrypted payload frames.
     */
    fun unwrapTotaFrames(stream: ByteArray, aesKey: ByteArray? = null): List<ByteArray> {
        return unwrapTotaFramesWithConsumed(stream, aesKey).first
    }

    /**
     * Encodes Client Hello (Step 1 of handshake).
     */
    fun encodeClientHello(clientRand: ByteArray): ByteArray {
        val lenBytes = packIntLe(clientRand.size + 1)
        val payload = ByteArray(5 + clientRand.size)
        payload[0] = 0x01
        payload[1] = 0x10
        payload[2] = lenBytes[0]
        payload[3] = lenBytes[1]
        payload[4] = clientRand.size.toByte()
        clientRand.copyInto(payload, destinationOffset = 5)
        return wrapTotaFrame(payload)
    }

    /**
     * Parses Server Hello (Step 2 of handshake) to extract the server random bytes.
     */
    fun parseServerHello(frame: ByteArray): ByteArray? {
        if (frame.size < 5) return null
        if (frame[0] != 0x02.toByte() || frame[1] != 0x10.toByte()) return null
        val len = (((frame[2].toInt() and 0xFF) or ((frame[3].toInt() and 0xFF) shl 8))) - 1
        if (len <= 0 || frame.size < 5 + len) return null
        return frame.copyOfRange(5, 5 + len)
    }

    /**
     * Encodes Client Verify (Step 3 of handshake).
     */
    fun encodeClientVerify(hash32: ByteArray): ByteArray {
        val payload = byteArrayOf(0x03, 0x10, 0x21, 0x00, 0x20) + hash32
        return wrapTotaFrame(payload)
    }

    /**
     * Verifies Server Success response (Step 4 of handshake).
     */
    fun isServerSuccess(frame: ByteArray): Boolean {
        if (frame.size < 4) return false
        val str = frame.decodeToString()
        return str.contains("suc")
    }
}
