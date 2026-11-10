package com.openhear.common.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.openhear.common.device.ActionType
import com.openhear.common.device.GestureType
import com.openhear.common.di.AppContainer
import com.openhear.common.ui.components.OpenHearIcons
import com.openhear.common.ui.components.ThemeSelectorDropdown
import kotlinx.coroutines.launch

class TouchMappingScreen : Screen {

    companion object {
        private const val PREF_PREFIX = "openhear_gesture_"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val settings = AppContainer.settings
        val activeDevice = AppContainer.deviceConnectionManager.getActiveDevice()
        val snackbarHostState = remember { SnackbarHostState() }

        var selectedEarbudTab by remember { mutableStateOf(0) }
        val isRight = selectedEarbudTab == 1
        val earbudKey = if (isRight) "right" else "left"

        var singleTapAction by remember(selectedEarbudTab) {
            mutableStateOf(loadAction(earbudKey, GestureType.SINGLE_TAP, defaultFor(isRight, GestureType.SINGLE_TAP)))
        }
        var doubleTapAction by remember(selectedEarbudTab) {
            mutableStateOf(loadAction(earbudKey, GestureType.DOUBLE_TAP, defaultFor(isRight, GestureType.DOUBLE_TAP)))
        }
        var longPressAction by remember(selectedEarbudTab) {
            mutableStateOf(loadAction(earbudKey, GestureType.LONG_PRESS, defaultFor(isRight, GestureType.LONG_PRESS)))
        }

        fun saveAndApply(gesture: GestureType, newAction: ActionType, oldAction: ActionType, updateLocal: (ActionType) -> Unit) {
            updateLocal(newAction)
            coroutineScope.launch {
                try {
                    val result = activeDevice?.remapGesture(gesture, newAction, isRightEarbud = isRight)
                    if (result?.isSuccess == true) {
                        settings.putString("${PREF_PREFIX}${earbudKey}_${gesture.name}", newAction.name)
                        snackbarHostState.showSnackbar(
                            "Updated ${gestureLabel(gesture)} to ${actionLabel(newAction)}"
                        )
                    } else {
                        updateLocal(oldAction)
                        snackbarHostState.showSnackbar("Failed to update gesture. Device may be out of range.")
                    }
                } catch (e: Exception) {
                    updateLocal(oldAction)
                    snackbarHostState.showSnackbar("Error updating gesture.")
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Touch Controls", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    },
                    actions = {
                        ThemeSelectorDropdown()
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedEarbudTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedEarbudTab == 0,
                        onClick = { selectedEarbudTab = 0 },
                        text = {
                            Text(
                                "Left Earbud",
                                fontWeight = if (selectedEarbudTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedEarbudTab == 1,
                        onClick = { selectedEarbudTab = 1 },
                        text = {
                            Text(
                                "Right Earbud",
                                fontWeight = if (selectedEarbudTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Gesture Assignments",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GestureDropdown(
                            title = "Single Tap",
                            description = "Triggered on a single light touch",
                            selectedAction = singleTapAction,
                            onActionSelected = {
                                saveAndApply(GestureType.SINGLE_TAP, it, singleTapAction) { newAct -> singleTapAction = newAct }
                            }
                        )

                        GestureDropdown(
                            title = "Double Tap",
                            description = "Triggered on two quick taps",
                            selectedAction = doubleTapAction,
                            onActionSelected = {
                                saveAndApply(GestureType.DOUBLE_TAP, it, doubleTapAction) { newAct -> doubleTapAction = newAct }
                            }
                        )

                        GestureDropdown(
                            title = "Long Press",
                            description = "Press and hold for 2 seconds",
                            selectedAction = longPressAction,
                            onActionSelected = {
                                saveAndApply(GestureType.LONG_PRESS, it, longPressAction) { newAct -> longPressAction = newAct }
                            }
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = OpenHearIcons.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Beast Mode Hardware Trigger",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Assign Beast Mode to a gesture above to toggle 50ms low-latency mode directly from your earbuds.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GestureDropdown(
        title: String,
        description: String,
        selectedAction: ActionType,
        onActionSelected: (ActionType) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        val availableActions = listOf(
            ActionType.PLAY_PAUSE to "Play / Pause",
            ActionType.NEXT_TRACK to "Next Track",
            ActionType.PREVIOUS_TRACK to "Previous Track",
            ActionType.VOLUME_UP to "Volume Up",
            ActionType.VOLUME_DOWN to "Volume Down",
            ActionType.VOICE_ASSISTANT to "Google Assistant",
            ActionType.TOGGLE_LOW_LATENCY to "Beast Mode",
            ActionType.NONE to "None"
        )

        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable { expanded = true },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            supportingContent = {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Box {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .width(152.dp)
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = actionLabel(selectedAction),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableActions.forEach { (action, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (action == selectedAction) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onActionSelected(action)
                                    expanded = false
                                },
                                trailingIcon = if (action == selectedAction) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        )
    }

    private fun loadAction(earbud: String, gesture: GestureType, defaultAction: ActionType): ActionType {
        val saved = AppContainer.settings.getString("${PREF_PREFIX}${earbud}_${gesture.name}", defaultAction.name)
        return try {
            ActionType.valueOf(saved)
        } catch (e: Exception) {
            defaultAction
        }
    }

    private fun defaultFor(isRight: Boolean, gesture: GestureType): ActionType {
        return when (gesture) {
            GestureType.SINGLE_TAP -> ActionType.PLAY_PAUSE
            GestureType.DOUBLE_TAP -> if (isRight) ActionType.NEXT_TRACK else ActionType.PREVIOUS_TRACK
            GestureType.LONG_PRESS -> ActionType.TOGGLE_LOW_LATENCY
        }
    }

    private fun actionLabel(action: ActionType): String {
        return when (action) {
            ActionType.PLAY_PAUSE -> "Play / Pause"
            ActionType.NEXT_TRACK -> "Next Track"
            ActionType.PREVIOUS_TRACK -> "Previous Track"
            ActionType.VOLUME_UP -> "Volume Up"
            ActionType.VOLUME_DOWN -> "Volume Down"
            ActionType.VOICE_ASSISTANT -> "Google Assistant"
            ActionType.TOGGLE_LOW_LATENCY -> "Beast Mode"
            ActionType.NONE -> "None"
        }
    }

    private fun gestureLabel(gesture: GestureType): String {
        return when (gesture) {
            GestureType.SINGLE_TAP -> "Single Tap"
            GestureType.DOUBLE_TAP -> "Double Tap"
            GestureType.LONG_PRESS -> "Long Press"
        }
    }
}
