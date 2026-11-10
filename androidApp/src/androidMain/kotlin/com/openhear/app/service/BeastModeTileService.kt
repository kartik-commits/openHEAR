package com.openhear.app.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.openhear.common.device.Airdopes800Hearable
import com.openhear.common.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class BeastModeTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val activeDevice = AppContainer.deviceConnectionManager.getActiveDevice()
        if (activeDevice is Airdopes800Hearable) {
            val currentState = activeDevice.isLowLatencyEnabled.value
            scope.launch {
                activeDevice.setLowLatencyMode(!currentState)
                updateTileState()
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val activeDevice = AppContainer.deviceConnectionManager.getActiveDevice()

        if (activeDevice is Airdopes800Hearable) {
            val isLowLatency = activeDevice.isLowLatencyEnabled.value
            tile.state = if (isLowLatency) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = if (isLowLatency) "Beast Mode: ON" else "Beast Mode: OFF"
        } else {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Beast Mode"
        }
        tile.updateTile()
    }
}
