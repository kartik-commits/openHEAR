package com.openhear.common.protocol

/**
 * CRC-16 calculation utility for Bluetooth packet frame verification.
 * Implements CRC-16/CCITT (polynomial 0x1021, initial value 0xFFFF).
 */
object Crc16 {
    private const val POLYNOMIAL = 0x1021
    private const val INITIAL_VALUE = 0xFFFF

    fun compute(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size - offset): Int {
        var crc = INITIAL_VALUE
        for (i in offset until (offset + length)) {
            val b = bytes[i].toInt() and 0xFF
            crc = crc xor (b shl 8)
            for (j in 0 until 8) {
                crc = if ((crc and 0x8000) != 0) {
                    (crc shl 1) xor POLYNOMIAL
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }
        return crc
    }

    fun verify(bytes: ByteArray, expectedCrc: Int, offset: Int = 0, length: Int = bytes.size - offset): Boolean {
        return compute(bytes, offset, length) == (expectedCrc and 0xFFFF)
    }
}
