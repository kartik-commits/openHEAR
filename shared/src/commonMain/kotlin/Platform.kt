package com.openhear.common

import com.openhear.common.device.DiscoveredDevice

expect fun getPlatformName(): String

expect fun getPairedHearables(): List<DiscoveredDevice>

expect fun getSystemBatteryPercentage(macAddress: String): Int?
