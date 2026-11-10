package com.openhear.common.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openhear.common.ui.theme.ThemeMode
import com.openhear.common.ui.theme.ThemeState

/**
 * Top app bar theme switcher dropdown supporting:
 * - Normal (Light)
 * - Dark Mode (Warm dark / IPS)
 * - Pitch Dark Mode (Pure black / OLED)
 */
@Composable
fun ThemeSelectorDropdown(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val currentMode = ThemeState.currentMode

    val triggerIcon = when (currentMode) {
        ThemeMode.LIGHT -> OpenHearIcons.Sun
        ThemeMode.DARK -> OpenHearIcons.Moon
        ThemeMode.PITCH_BLACK -> OpenHearIcons.Eclipse
    }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = triggerIcon,
                contentDescription = "Current theme: ${currentMode.label}. Tap to change.",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeMode.entries.forEach { mode ->
                val isSelected = mode == currentMode
                val modeIcon = when (mode) {
                    ThemeMode.LIGHT -> OpenHearIcons.Sun
                    ThemeMode.DARK -> OpenHearIcons.Moon
                    ThemeMode.PITCH_BLACK -> OpenHearIcons.Eclipse
                }

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        ThemeState.setMode(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}
