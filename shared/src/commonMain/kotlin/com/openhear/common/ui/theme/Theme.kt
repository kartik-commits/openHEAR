package com.openhear.common.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.openhear.common.di.AppContainer

enum class ThemeMode(val label: String, val description: String) {
    LIGHT("Normal", "Light appearance"),
    DARK("Dark Mode", "Warm dark for IPS / LCD"),
    PITCH_BLACK("Pitch Dark Mode", "True black for OLED")
}

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
)

// Pitch black = the warm dark scheme with true-black surfaces for OLED.
private val PitchDarkColorScheme = DarkColorScheme.copy(
    background = Color(0xFF000000),
    onBackground = Color(0xFFEDE0DE),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFEDE0DE),
    surfaceVariant = Color(0xFF1C1615),
    onSurfaceVariant = Color(0xFFD8C2BF),
    outline = Color(0xFF4A403F),
    outlineVariant = Color(0xFF282020),
)

object ThemeState {
    private const val PREF_MODE_KEY = "openhear_theme_mode"
    private const val PREF_LEGACY_KEY = "openhear_is_dark_theme"

    var currentMode by mutableStateOf(loadInitialMode())
        private set

    val isDarkTheme: Boolean
        get() = currentMode != ThemeMode.LIGHT

    fun setMode(mode: ThemeMode) {
        currentMode = mode
        AppContainer.settings.putString(PREF_MODE_KEY, mode.name)
        AppContainer.settings.putBoolean(PREF_LEGACY_KEY, mode != ThemeMode.LIGHT)
    }

    fun toggle() {
        val next = when (currentMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.PITCH_BLACK
            ThemeMode.PITCH_BLACK -> ThemeMode.LIGHT
        }
        setMode(next)
    }

    private fun loadInitialMode(): ThemeMode {
        val saved = AppContainer.settings.getString(PREF_MODE_KEY, "")
        if (saved.isNotEmpty()) {
            return try {
                ThemeMode.valueOf(saved)
            } catch (e: Exception) {
                ThemeMode.DARK
            }
        }
        val legacyDark = AppContainer.settings.getBoolean(PREF_LEGACY_KEY, true)
        return if (legacyDark) ThemeMode.DARK else ThemeMode.LIGHT
    }
}

@Composable
fun OpenHearTheme(
    themeMode: ThemeMode = ThemeState.currentMode,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.PITCH_BLACK -> PitchDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = OpenHearShapes,
        typography = OpenHearTypography,
        content = content
    )
}
