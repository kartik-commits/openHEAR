package com.openhear.common.protocol

expect fun sha256(data: ByteArray): ByteArray

expect fun aesCbcEncrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray

expect fun aesCbcDecrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray
