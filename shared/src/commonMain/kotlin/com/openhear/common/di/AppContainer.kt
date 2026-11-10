package com.openhear.common.di

import com.openhear.common.ble.BleScanner
import com.openhear.common.device.DeviceConnectionManager
import com.russhwolf.settings.Settings

/**
 * Manual Dependency Injection Container.
 *
 * Instead of relying on Koin/Dagger, we manually instantiate singleton
 * dependencies here and provide them to our ViewModels or UI factories.
 */
object AppContainer {

    // Singleton Settings (Key-Value persistence)
    val settings: Settings by lazy {
        Settings()
    }

    // Singleton BLE Scanner
    val bleScanner: BleScanner by lazy {
        BleScanner()
    }

    // Singleton instance for device connection management
    val deviceConnectionManager: DeviceConnectionManager by lazy {
        DeviceConnectionManager()
    }
}
