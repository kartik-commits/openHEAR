package com.openhear.common.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.openhear.common.di.AppContainer
import com.openhear.common.ui.theme.Dimensions
import kotlinx.coroutines.launch

class EqScreen : Screen {

    companion object {
        private const val PREF_PRESET = "openhear_eq_preset"
        private const val PREF_BAND_PREFIX = "openhear_eq_band_"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val settings = AppContainer.settings
        val activeDevice = AppContainer.deviceConnectionManager.getActiveDevice()

        val presets = listOf("Signature Sound", "Bass Boost", "Vocal Focus", "Treble Boost", "Custom")
        val bandLabels = listOf("60 Hz", "120 Hz", "250 Hz", "750 Hz", "2 kHz", "5 kHz", "15 kHz")

        var selectedPreset by remember {
            mutableStateOf(settings.getString(PREF_PRESET, "Signature Sound"))
        }

        var bands by remember {
            mutableStateOf(
                List(7) { i ->
                    settings.getFloat("$PREF_BAND_PREFIX$i", getDefaultBand(selectedPreset, i))
                }
            )
        }

        val snackbarHostState = remember { SnackbarHostState() }

        fun applyBands(newBands: List<Float>, preset: String) {
            val oldBands = bands.toList()
            val oldPreset = selectedPreset

            bands = newBands
            selectedPreset = preset

            coroutineScope.launch {
                try {
                    val result = activeDevice?.setEqualizer(newBands.map { it.toInt() })
                    if (result?.isSuccess == true) {
                        settings.putString(PREF_PRESET, preset)
                        newBands.forEachIndexed { index, value ->
                            settings.putFloat("$PREF_BAND_PREFIX$index", value)
                        }
                    } else {
                        bands = oldBands
                        selectedPreset = oldPreset
                        snackbarHostState.showSnackbar("Failed to apply equalizer. Device may be disconnected.")
                    }
                } catch (e: Exception) {
                    bands = oldBands
                    selectedPreset = oldPreset
                    snackbarHostState.showSnackbar("Error applying equalizer.")
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text("Equalizer", style = MaterialTheme.typography.titleLarge)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { applyBands(List(7) { 0f }, "Custom") }
                        ) {
                            Text("Reset")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimensions.space24, vertical = Dimensions.space16),
                verticalArrangement = Arrangement.spacedBy(Dimensions.space32)
            ) {
                // Interactive Frequency Response Curve Preview
                EqCurveVisualizer(bands = bands)

                // Preset Selection Chips
                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.space16)) {
                    Text(
                        text = "Sound Profiles",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.space8)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = preset == selectedPreset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val presetBands = when (preset) {
                                        "Bass Boost" -> listOf(5f, 4f, 3f, 1f, 0f, 0f, -1f)
                                        "Signature Sound" -> listOf(2f, 1f, 0f, 0f, 1f, 2f, 3f)
                                        "Vocal Focus" -> listOf(-1f, 0f, 1f, 4f, 3f, 1f, 0f)
                                        "Treble Boost" -> listOf(0f, 0f, 0f, 1f, 3f, 4f, 5f)
                                        else -> bands
                                    }
                                    applyBands(presetBands, preset)
                                },
                                label = { Text(preset) }
                            )
                        }
                    }
                }

                // 7-Band Equalizer Sliders Section
                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.space16)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Tuning",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "-5 dB to +5 dB",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.space12)
                    ) {
                        bands.forEachIndexed { index, value ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = Dimensions.minTouchTarget),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bandLabels.getOrElse(index) { "Band $index" },
                                    modifier = Modifier.width(64.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Slider(
                                    value = value,
                                    onValueChange = { newValue ->
                                        val updated = bands.toMutableList().also { it[index] = newValue }
                                        applyBands(updated, "Custom")
                                    },
                                    valueRange = -5f..5f,
                                    steps = 9,
                                    modifier = Modifier.weight(1f)
                                )

                                val dbText = when {
                                    value > 0 -> "+${value.toInt()} dB"
                                    else -> "${value.toInt()} dB"
                                }

                                Text(
                                    text = dbText,
                                    modifier = Modifier.width(56.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (value.toInt() != 0)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EqCurveVisualizer(
        bands: List<Float>,
        modifier: Modifier = Modifier
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val outlineColor = MaterialTheme.colorScheme.outlineVariant
        val surfaceColor = MaterialTheme.colorScheme.surface

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimensions.space24, vertical = Dimensions.space24)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f

                // Center 0dB reference line
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 1.dp.toPx()
                )

                if (bands.isEmpty()) return@Canvas

                val stepX = width / (bands.size + 1)
                val points = bands.mapIndexed { index, db ->
                    val x = stepX * (index + 1)
                    val normalized = ((db + 5f) / 10f).coerceIn(0f, 1f)
                    val y = height * (1f - normalized)
                    Offset(x, y)
                }

                // Draw smooth interpolated curve
                val strokePath = Path().apply {
                    moveTo(0f, centerY)
                    lineTo(points.first().x * 0.4f, (centerY + points.first().y) / 2f)

                    for (i in 0 until points.size - 1) {
                        val p0 = if (i > 0) points[i - 1] else points[i]
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val p3 = if (i + 2 < points.size) points[i + 2] else p2

                        val cp1x = p1.x + (p2.x - p0.x) / 5f
                        val cp1y = p1.y + (p2.y - p0.y) / 5f
                        val cp2x = p2.x - (p3.x - p1.x) / 5f
                        val cp2y = p2.y - (p3.y - p1.y) / 5f

                        cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                    }

                    val lastPoint = points.last()
                    lineTo(width - (width - lastPoint.x) * 0.4f, (centerY + lastPoint.y) / 2f)
                    lineTo(width, centerY)
                }

                // Subtle gradient fill under curve
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(width, centerY)
                    lineTo(0f, centerY)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.2f),
                            primaryColor.copy(alpha = 0.0f)
                        )
                    )
                )

                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Frequency response point dots
                points.forEach { pt ->
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = surfaceColor,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }
    }

    private fun getDefaultBand(preset: String, index: Int): Float {
        return when (preset) {
            "Bass Boost" -> listOf(5f, 4f, 3f, 1f, 0f, 0f, -1f).getOrElse(index) { 0f }
            "Signature Sound" -> listOf(2f, 1f, 0f, 0f, 1f, 2f, 3f).getOrElse(index) { 0f }
            "Vocal Focus" -> listOf(-1f, 0f, 1f, 4f, 3f, 1f, 0f).getOrElse(index) { 0f }
            "Treble Boost" -> listOf(0f, 0f, 0f, 1f, 3f, 4f, 5f).getOrElse(index) { 0f }
            else -> 0f
        }
    }
}
