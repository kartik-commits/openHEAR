package com.openhear.common.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openhear.common.ui.theme.ThemeState
import com.openhear.common.ui.theme.success
import com.openhear.common.ui.theme.successDark
import com.openhear.common.ui.theme.warning
import com.openhear.common.ui.theme.warningDark

/**
 * Clean, meaningful battery indicator prioritizing functional feedback
 * and standard Material 3 color semantics.
 */
@Composable
fun Material3BatteryItem(
    label: String,
    percent: Int?,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val pct = percent ?: 0

    val isDark = ThemeState.isDarkTheme

    val isOffline = percent == null

    // Semantic colors derived from standard roles
    val activeSegmentColor = when {
        isOffline -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        pct > 50 -> if (isDark) successDark else success
        pct > 20 -> if (isDark) warningDark else warning
        else -> MaterialTheme.colorScheme.error
    }

    // Number of active segments out of 4
    val activeSegments = when {
        isOffline -> 0
        pct >= 85 -> 4
        pct >= 60 -> 3
        pct >= 35 -> 2
        pct > 0 -> 1
        else -> 0
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (!isOffline) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = if (isOffline) "Offline" else "$pct%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (!isOffline)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                val isLit = i <= activeSegments
                val segmentColor by animateColorAsState(
                    targetValue = if (isLit) activeSegmentColor else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = tween(durationMillis = 300)
                )

                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 4.dp)
                        .background(segmentColor, RoundedCornerShape(2.dp))
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
