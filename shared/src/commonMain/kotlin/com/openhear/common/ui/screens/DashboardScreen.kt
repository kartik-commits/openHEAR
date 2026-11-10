package com.openhear.common.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.openhear.common.device.ConnectionStatus
import com.openhear.common.di.AppContainer
import com.openhear.common.ui.components.Material3BatteryItem
import com.openhear.common.ui.components.OpenHearIcons
import com.openhear.common.ui.components.ThemeSelectorDropdown
import com.openhear.common.ui.theme.Dimensions
import com.openhear.common.ui.theme.ThemeState
import com.openhear.common.ui.theme.success
import com.openhear.common.ui.theme.successDark
import kotlinx.coroutines.launch

class DashboardScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val connectionManager = AppContainer.deviceConnectionManager
        val activeDevice = connectionManager.getActiveDevice()
        val snackbarHostState = remember { SnackbarHostState() }

        val connectionState by connectionManager.globalConnectionState.collectAsState()
        val batteryState by (activeDevice?.batteryState ?: connectionManager.globalBatteryState).collectAsState()

        val isBeastModeEnabled by connectionManager.globalLowLatencyState.collectAsState()

        val activePreset = remember {
            AppContainer.settings.getString("openhear_eq_preset", "Signature Sound")
        }

        // Return to scan screen on disconnect, query hardware when connected
        LaunchedEffect(connectionState) {
            if (connectionState == ConnectionStatus.DISCONNECTED) {
                navigator.popUntil { it is ScanScreen }
            } else if (connectionState == ConnectionStatus.CONNECTED) {
                connectionManager.queryLowLatency()
            }
        }

        // Query hardware Beast Mode state on display
        LaunchedEffect(Unit) {
            connectionManager.queryLowLatency()
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "OpenHear",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        ThemeSelectorDropdown()
                    }
                )
            }
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val isWideScreen = maxWidth >= 800.dp

                if (isWideScreen) {
                    // Wide-screen / tablet two-pane layout
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.space32, vertical = Dimensions.space24),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.space48)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.space32)
                        ) {
                            DeviceHeroSection(
                                deviceName = activeDevice?.deviceName ?: "boAt Airdopes 800",
                                batteryState = batteryState
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.space32)
                        ) {
                            DeviceControlsSection(
                                activePreset = activePreset,
                                isBeastModeEnabled = isBeastModeEnabled,
                                onNavigateToEq = { navigator.push(EqScreen()) },
                                onNavigateToTouch = { navigator.push(TouchMappingScreen()) }
                            )
                            Spacer(Modifier.height(Dimensions.space16))
                            DisconnectButton(
                                onDisconnect = { coroutineScope.launch { connectionManager.disconnectCurrent() } }
                            )
                        }
                    }
                } else {
                    // Mobile single-column layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Dimensions.space24, vertical = Dimensions.space16),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.space32)
                    ) {
                        DeviceHeroSection(
                            deviceName = activeDevice?.deviceName ?: "boAt Airdopes 800",
                            batteryState = batteryState
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = Dimensions.space16)
                        )

                        DeviceControlsSection(
                            activePreset = activePreset,
                            isBeastModeEnabled = isBeastModeEnabled,
                            onNavigateToEq = { navigator.push(EqScreen()) },
                            onNavigateToTouch = { navigator.push(TouchMappingScreen()) }
                        )

                        Spacer(Modifier.height(Dimensions.space16))
                        DisconnectButton(
                            onDisconnect = { coroutineScope.launch { connectionManager.disconnectCurrent() } }
                        )
                        Spacer(Modifier.height(Dimensions.space24))
                    }
                }
            }
        }
    }

    @Composable
    private fun DeviceHeroSection(
        deviceName: String,
        batteryState: com.openhear.common.device.BatteryData
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.space24)
        ) {
            Image(
                imageVector = OpenHearIcons.Logo,
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.space8)
            ) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (ThemeState.isDarkTheme) successDark else success,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Battery Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.space16),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Material3BatteryItem(
                    label = "L",
                    percent = batteryState.leftPercent,
                    icon = OpenHearIcons.EarbudLeft,
                    modifier = Modifier.weight(1f)
                )
                Material3BatteryItem(
                    label = "Case",
                    percent = batteryState.casePercent,
                    icon = OpenHearIcons.ChargingCase,
                    modifier = Modifier.weight(1f)
                )
                Material3BatteryItem(
                    label = "R",
                    percent = batteryState.rightPercent,
                    icon = OpenHearIcons.EarbudRight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    @Composable
    private fun DeviceControlsSection(
        activePreset: String,
        isBeastModeEnabled: Boolean,
        onNavigateToEq: () -> Unit,
        onNavigateToTouch: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.space8)
        ) {
            Text(
                text = "Audio Features",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimensions.space8)
            )

            ListItem(
                headlineContent = {
                    Text("Equalizer & Sound Profile", style = MaterialTheme.typography.titleMedium)
                },
                supportingContent = {
                    Text(text = activePreset, style = MaterialTheme.typography.bodyMedium)
                },
                leadingContent = {
                    Icon(
                        imageVector = OpenHearIcons.Equalizer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onNavigateToEq() },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            ListItem(
                headlineContent = {
                    Text("Touch Controls", style = MaterialTheme.typography.titleMedium)
                },
                supportingContent = {
                    Text(text = "Customize gestures", style = MaterialTheme.typography.bodyMedium)
                },
                leadingContent = {
                    Icon(
                        imageVector = OpenHearIcons.Touch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onNavigateToTouch() },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "Beast Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                supportingContent = {
                    Text(
                        text = if (isBeastModeEnabled) "Active • 50ms latency" else "Standard latency • Toggle via earbud",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isBeastModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = OpenHearIcons.Bolt,
                        contentDescription = null,
                        tint = if (isBeastModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isBeastModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isBeastModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = if (isBeastModeEnabled) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isBeastModeEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }

    @Composable
    private fun DisconnectButton(onDisconnect: () -> Unit) {
        OutlinedButton(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Text(
                text = "Disconnect Device",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
