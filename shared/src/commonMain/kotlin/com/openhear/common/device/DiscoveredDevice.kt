package com.openhear.common.device

data class DiscoveredDevice(
    val name: String,
    val address: String,
    val rssi: Int = 0
)
