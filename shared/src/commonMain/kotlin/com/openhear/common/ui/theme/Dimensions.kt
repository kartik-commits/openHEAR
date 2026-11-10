package com.openhear.common.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard 4dp / 8dp grid spacing tokens.
 * Enforces visual rhythm and prevents arbitrary padding.
 */
object Dimensions {
    val space2: Dp = 2.dp
    val space4: Dp = 4.dp
    val space8: Dp = 8.dp
    val space12: Dp = 12.dp
    val space16: Dp = 16.dp
    val space20: Dp = 20.dp
    val space24: Dp = 24.dp
    val space32: Dp = 32.dp
    val space48: Dp = 48.dp

    // Interactive target standards (WCAG AAA / Android Accessibility)
    val minTouchTarget: Dp = 48.dp
    val iconSizeSmall: Dp = 18.dp
    val iconSizeMedium: Dp = 24.dp
}
