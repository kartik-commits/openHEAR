package com.openhear.common.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Coherent shape hierarchy adhering to Material 3:
 * - Small: controls, chips, small badges (8dp)
 * - Medium: buttons, standard tiles, cards (12dp)
 * - Large: bottom sheets, hero sections, larger containers (16dp)
 * - ExtraLarge: dialogs, major surfaces (20dp)
 */
val OpenHearShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)
