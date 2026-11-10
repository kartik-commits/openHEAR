package com.openhear.common.transport

import kotlinx.coroutines.flow.Flow

interface RfcommTransport {
    suspend fun connect(macAddress: String, channel: Int = 13): Result<Unit>
    suspend fun send(data: ByteArray): Result<Unit>
    fun observeIncoming(): Flow<ByteArray>
    suspend fun disconnect()
    val isConnected: Boolean
}

expect fun createRfcommTransport(): RfcommTransport
