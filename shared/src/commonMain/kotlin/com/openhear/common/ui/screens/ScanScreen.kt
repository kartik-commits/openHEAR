package com.openhear.common.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.openhear.common.device.ConnectionStatus
import com.openhear.common.device.DiscoveredDevice
import com.openhear.common.device.HearableFactory
import com.openhear.common.di.AppContainer
import com.openhear.common.ui.components.OpenHearIcons
import com.openhear.common.ui.components.ThemeSelectorDropdown
import com.openhear.common.ui.theme.ThemeState
import com.openhear.common.ui.theme.success
import com.openhear.common.ui.theme.successDark
import com.openhear.common.ui.theme.warning
import com.openhear.common.ui.theme.warningDark
import kotlinx.coroutines.launch

class ScanScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val bleScanner = AppContainer.bleScanner
        val connectionManager = AppContainer.deviceConnectionManager

        val discoveredDevices by bleScanner.discoveredDevices.collectAsState()
        val isScanning by bleScanner.isScanning.collectAsState()
        val connectionState by connectionManager.globalConnectionState.collectAsState()

        var connectingAddress by remember { mutableStateOf<String?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            bleScanner.startScan()
        }

        LaunchedEffect(connectionState) {
            if (connectionState == ConnectionStatus.CONNECTED) {
                connectingAddress = null
                navigator.push(DashboardScreen())
            } else if (connectionState == ConnectionStatus.ERROR) {
                connectingAddress = null
                snackbarHostState.showSnackbar("Failed to connect. Ensure device is nearby.")
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text("OpenHear", style = MaterialTheme.typography.titleLarge)
                        },
                        actions = {
                            ThemeSelectorDropdown()
                            IconButton(
                                onClick = { bleScanner.startScan() },
                                enabled = !isScanning && connectionState != ConnectionStatus.CONNECTING
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Rescan",
                                    tint = if (isScanning) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                    if (isScanning || connectionState == ConnectionStatus.CONNECTING) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEARBY DEVICES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isScanning) "Scanning…" else "${discoveredDevices.size} found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (discoveredDevices.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Image(
                                imageVector = OpenHearIcons.Logo,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isScanning) "Searching for Devices" else "No Devices Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isScanning)
                                        "Keep your hearables out of their case and within range."
                                    else
                                        "Ensure earbuds are out of their case, then scan again.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            if (!isScanning) {
                                Button(onClick = { bleScanner.startScan() }) {
                                    Text("Scan Again")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(discoveredDevices) { device ->
                            val isConnectingThis = connectingAddress == device.address
                            DeviceRow(
                                device = device,
                                isConnecting = isConnectingThis,
                                onConnect = {
                                    connectingAddress = device.address
                                    coroutineScope.launch {
                                        val ad = bleScanner.getAdvertisement(device.address)
                                        val hearable = HearableFactory.create(device.name, ad)
                                        connectionManager.connectToDevice(hearable, device.address)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DeviceRow(
        device: DiscoveredDevice,
        isConnecting: Boolean,
        onConnect: () -> Unit
    ) {
        ListItem(
            headlineContent = {
                Text(device.name, style = MaterialTheme.typography.titleMedium)
            },
            supportingContent = {
                Text(device.address, style = MaterialTheme.typography.bodySmall)
            },
            leadingContent = {
                Icon(
                    imageVector = OpenHearIcons.Headphones,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                if (isConnecting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Connecting",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    FilledTonalButton(onClick = onConnect) {
                        Text("Connect")
                    }
                }
            },
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable(enabled = !isConnecting) { onConnect() },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }

}
