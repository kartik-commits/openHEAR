package com.openhear.common.ble

import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.openhear.common.device.DiscoveredDevice
import com.openhear.common.getPairedHearables
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class BleScanner(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val advertisementCache = mutableMapOf<String, Advertisement>()
    private var scanJob: Job? = null

    fun startScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        advertisementCache.clear()

        // Seed with already paired hearables from system
        val paired = getPairedHearables().filter {
            it.name.contains("Airdopes", ignoreCase = true) ||
            it.name.contains("OpenHear", ignoreCase = true) ||
            it.name.contains("Hear", ignoreCase = true) ||
            it.name.contains("800", ignoreCase = true)
        }
        _discoveredDevices.value = paired
        paired.forEach { device ->
            Napier.i("Found already paired hearable: ${device.name} [${device.address}]")
        }

        Napier.i("Starting BLE Scan...")

        scanJob = scope.launch {
            try {
                val scanner = Scanner()
                scanner.advertisements.collect { ad ->
                    val name = ad.name ?: ad.peripheralName ?: "Unknown Device"
                    val address = ad.identifier
                    val rssi = ad.rssi

                    advertisementCache[address.lowercase()] = ad

                    val currentList = _discoveredDevices.value.toMutableList()
                    val existingIndex = currentList.indexOfFirst { it.address.equals(address, ignoreCase = true) }

                    val updatedDevice = DiscoveredDevice(name = name, address = address, rssi = rssi)
                    if (existingIndex >= 0) {
                        currentList[existingIndex] = updatedDevice
                    } else {
                        currentList.add(updatedDevice)
                    }

                    _discoveredDevices.value = currentList
                }
            } catch (e: Throwable) {
                Napier.w("BLE scan encountered an error or Bluetooth is unavailable: ${e.message}")
                // Fallback: Add a mock preview device if no physical devices could be found
                if (_discoveredDevices.value.isEmpty()) {
                    _discoveredDevices.value = listOf(
                        DiscoveredDevice(
                            name = "Airdopes 800 HiDef (Preview)",
                            address = "00:11:22:33:44:55",
                            rssi = -55
                        )
                    )
                }
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun stopScan() {
        Napier.i("Stopping BLE Scan...")
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }

    fun getAdvertisement(address: String): Advertisement? {
        return advertisementCache[address.lowercase()]
    }
}
