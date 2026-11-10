package com.openhear.common.protocol

/**
 * IEEE 802.3 32-bit Cyclic Redundancy Check (CRC-32) utility.
 * Standard polynomial 0xEDB88320 (reversed representation of 0x04C11DB7).
 */
object Crc32 {
    private val TABLE = IntArray(256) { i ->
        var c = i
        for (j in 0 until 8) {
            c = if ((c and 1) != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1
        }
        c
    }

    fun compute(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size - offset): Long {
        var c = 0xFFFFFFFF.toInt()
        for (i in offset until (offset + length)) {
            val idx = (c xor bytes[i].toInt()) and 0xFF
            c = (c ushr 8) xor TABLE[idx]
        }
        return (c xor 0xFFFFFFFF.toInt()).toLong() and 0xFFFFFFFFL
    }

    fun verify(bytes: ByteArray, expectedCrc: Long, offset: Int = 0, length: Int = bytes.size - offset): Boolean {
        return compute(bytes, offset, length) == expectedCrc
    }
}
