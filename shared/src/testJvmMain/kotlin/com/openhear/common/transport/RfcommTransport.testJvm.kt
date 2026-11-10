package com.openhear.common.transport

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual fun createRfcommTransport(): RfcommTransport = TestJvmRfcommTransport()

private class TestJvmRfcommTransport : RfcommTransport {
    override val isConnected = false

    override suspend fun connect(macAddress: String, channel: Int): Result<Unit> =
        Result.failure(UnsupportedOperationException("RFCOMM is unavailable on test JVM"))

    override suspend fun send(data: ByteArray): Result<Unit> =
        Result.failure(UnsupportedOperationException("RFCOMM is unavailable on test JVM"))

    override fun observeIncoming(): Flow<ByteArray> = emptyFlow()

    override suspend fun disconnect() = Unit
}
