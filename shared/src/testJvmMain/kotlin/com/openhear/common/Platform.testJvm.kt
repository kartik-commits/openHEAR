package com.openhear.common

import com.openhear.common.device.DiscoveredDevice

actual fun getPlatformName(): String = "Test JVM"

actual fun getPairedHearables(): List<DiscoveredDevice> = emptyList()

actual fun getSystemBatteryPercentage(macAddress: String): Int? = null
