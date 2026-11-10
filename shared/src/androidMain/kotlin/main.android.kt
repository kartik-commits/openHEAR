package com.openhear.common

import App
import androidx.compose.runtime.Composable
import com.openhear.common.device.DiscoveredDevice

actual fun getPlatformName(): String = "Android"

actual fun getPairedHearables(): List<DiscoveredDevice> = emptyList()

actual fun getSystemBatteryPercentage(macAddress: String): Int? = null

@Composable fun MainView() = App()
