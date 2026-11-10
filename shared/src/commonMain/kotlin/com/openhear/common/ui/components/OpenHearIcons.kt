package com.openhear.common.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Clean Material 3 vector icons for OpenHEAR without bloated dependencies.
 */
object OpenHearIcons {

    val Logo: ImageVector by lazy {
        ImageVector.Builder(
            name = "OpenHearLogo",
            defaultWidth = 108.dp,
            defaultHeight = 108.dp,
            viewportWidth = 108f,
            viewportHeight = 108f
        ).apply {
            path(fill = SolidColor(Color(0xFF243140))) {
                moveTo(24f, 0f)
                lineTo(84f, 0f)
                quadTo(108f, 0f, 108f, 24f)
                lineTo(108f, 84f)
                quadTo(108f, 108f, 84f, 108f)
                lineTo(24f, 108f)
                quadTo(0f, 108f, 0f, 84f)
                lineTo(0f, 24f)
                quadTo(0f, 0f, 24f, 0f)
                close()
            }

            path(
                stroke = SolidColor(Color(0xFFD2F0D5)),
                strokeLineWidth = 5.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(38.56f, 51.65f)
                arcTo(15.5f, 15.5f, 0f, true, true, 54f, 68.5f)
            }

            path(
                stroke = SolidColor(Color(0xFFB8D4DC)),
                strokeLineWidth = 5.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(74.35f, 40.28f)
                arcTo(24f, 24f, 0f, false, true, 53.16f, 76.99f)
            }

            path(
                stroke = SolidColor(Color(0xFFACC79E)),
                strokeLineWidth = 5.5f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(46.58f, 75.83f)
                arcTo(24f, 24f, 0f, true, true, 66.72f, 32.65f)
            }

            path(
                stroke = SolidColor(Color(0xFF94B48B)),
                strokeLineWidth = 6.2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(42.5f, 55f)
                lineTo(46.58f, 75.83f)
            }

            path(fill = SolidColor(Color(0xFF84A57F))) {
                moveTo(44.5f, 45.5f)
                horizontalLineTo(52f)
                verticalLineTo(60.5f)
                horizontalLineTo(44.5f)
                close()
            }

            path(fill = SolidColor(Color(0xFFC5E2DE))) {
                moveTo(52f, 45.5f)
                arcTo(7.5f, 7.5f, 0f, false, true, 52f, 60.5f)
                close()
            }

            path(fill = SolidColor(Color(0xFF9AB892))) {
                moveTo(44.5f, 45.5f)
                arcTo(7.5f, 7.5f, 0f, true, true, 44.5f, 60.5f)
                arcTo(7.5f, 7.5f, 0f, true, true, 44.5f, 45.5f)
                close()
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9.29f, 6.71f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
                lineTo(13.17f, 12f)
                lineToRelative(-3.88f, 3.88f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
                lineToRelative(4.59f, -4.59f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
                lineTo(10.7f, 6.7f)
                curveToRelative(-0.38f, -0.38f, -1.02f, -0.38f, -1.41f, 0.01f)
                close()
            }
        }.build()
    }

    val Headphones: ImageVector by lazy {
        ImageVector.Builder(
            name = "Headphones",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 3f)
                curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f)
                verticalLineToRelative(7f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(-8f)
                horizontalLineTo(5f)
                verticalLineToRelative(-2f)
                curveToRelative(0f, -3.87f, 3.13f, -7f, 7f, -7f)
                reflectiveCurveToRelative(7f, 3.13f, 7f, 7f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(-4f)
                verticalLineToRelative(8f)
                horizontalLineToRelative(3f)
                curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
                verticalLineToRelative(-7f)
                curveToRelative(0f, -4.97f, -4.03f, -9f, -9f, -9f)
                close()
            }
        }.build()
    }

    val Bluetooth: ImageVector by lazy {
        ImageVector.Builder(
            name = "Bluetooth",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(17.71f, 7.71f)
                lineTo(12f, 2f)
                horizontalLineToRelative(-1f)
                verticalLineToRelative(7.59f)
                lineTo(6.41f, 5.7f)
                lineTo(5f, 7.11f)
                lineTo(10.59f, 12.7f)
                lineTo(5f, 18.29f)
                lineToRelative(1.41f, 1.41f)
                lineTo(11f, 14.41f)
                verticalLineTo(22f)
                horizontalLineToRelative(1f)
                lineToRelative(5.71f, -5.71f)
                lineToRelative(-4.3f, -4.29f)
                lineToRelative(4.3f, -4.29f)
                close()
                moveTo(13f, 5.83f)
                lineToRelative(1.88f, 1.88f)
                lineTo(13f, 9.59f)
                verticalLineTo(5.83f)
                close()
                moveTo(14.88f, 16.29f)
                lineTo(13f, 18.17f)
                verticalLineToRelative(-3.76f)
                lineToRelative(1.88f, 1.88f)
                close()
            }
        }.build()
    }

    val Equalizer: ImageVector by lazy {
        ImageVector.Builder(
            name = "Equalizer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(10f, 20f)
                horizontalLineToRelative(4f)
                verticalLineTo(4f)
                horizontalLineToRelative(-4f)
                verticalLineToRelative(16f)
                close()
                moveTo(4f, 20f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(-7f)
                horizontalLineTo(4f)
                verticalLineToRelative(7f)
                close()
                moveTo(16f, 9f)
                verticalLineToRelative(11f)
                horizontalLineToRelative(4f)
                verticalLineTo(9f)
                horizontalLineToRelative(-4f)
                close()
            }
        }.build()
    }

    val Touch: ImageVector by lazy {
        ImageVector.Builder(
            name = "Touch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9f, 11.24f)
                verticalLineTo(7.5f)
                curveTo(9f, 6.12f, 10.12f, 5f, 11.5f, 5f)
                reflectiveCurveTo(14f, 6.12f, 14f, 7.5f)
                verticalLineToRelative(3.74f)
                curveToRelative(1.21f, -0.81f, 2f, -2.18f, 2f, -3.74f)
                curveToRelative(0f, -2.49f, -2.01f, -4.5f, -4.5f, -4.5f)
                reflectiveCurveTo(7f, 5.01f, 7f, 7.5f)
                curveToRelative(0f, 1.56f, 0.79f, 2.93f, 2f, 3.74f)
                close()
                moveTo(18.84f, 15.25f)
                lineToRelative(-4.58f, -2.29f)
                curveToRelative(-0.28f, -0.14f, -0.58f, -0.21f, -0.89f, -0.21f)
                horizontalLineTo(13f)
                verticalLineTo(7.5f)
                curveToRelative(0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
                reflectiveCurveTo(10f, 6.67f, 10f, 7.5f)
                verticalLineToRelative(10.74f)
                lineToRelative(-3.61f, -0.75f)
                curveToRelative(-0.09f, -0.02f, -0.18f, -0.03f, -0.27f, -0.03f)
                curveToRelative(-0.39f, 0f, -0.75f, 0.16f, -1.01f, 0.42f)
                lineToRelative(-0.73f, 0.74f)
                lineToRelative(5.15f, 5.15f)
                curveToRelative(0.42f, 0.42f, 0.99f, 0.65f, 1.59f, 0.65f)
                horizontalLineToRelative(6.46f)
                curveToRelative(1.04f, 0f, 1.91f, -0.76f, 2.04f, -1.79f)
                lineToRelative(0.72f, -5.58f)
                curveToRelative(0.12f, -0.92f, -0.34f, -1.82f, -1.15f, -2.25f)
                close()
            }
        }.build()
    }

    val Bolt: ImageVector by lazy {
        ImageVector.Builder(
            name = "Bolt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(11f, 21f)
                horizontalLineToRelative(-1f)
                lineToRelative(1f, -7f)
                horizontalLineTo(7.5f)
                curveToRelative(-0.58f, 0f, -0.57f, -0.32f, -0.38f, -0.66f)
                curveToRelative(0.19f, -0.34f, 0.05f, -0.08f, 0.07f, -0.12f)
                curveTo(8.22f, 11.45f, 10.66f, 7.15f, 14.5f, 3f)
                horizontalLineToRelative(1f)
                lineToRelative(-1f, 7f)
                horizontalLineToRelative(3.5f)
                curveToRelative(0.49f, 0f, 0.56f, 0.33f, 0.47f, 0.51f)
                lineToRelative(-0.07f, 0.15f)
                curveTo(15.42f, 15.64f, 12.98f, 19.94f, 11f, 21f)
                close()
            }
        }.build()
    }

    val Sun: ImageVector by lazy {
        ImageVector.Builder(
            name = "Sun",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 7f)
                curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
                reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
                reflectiveCurveToRelative(5f, -2.24f, 5f, -5f)
                reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
                close()
                moveTo(2f, 13f)
                horizontalLineToRelative(2f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f)
                horizontalLineTo(2f)
                curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
                reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
                close()
                moveTo(20f, 13f)
                horizontalLineToRelative(2f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f)
                horizontalLineToRelative(-2f)
                curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
                reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
                close()
                moveTo(11f, 2f)
                verticalLineToRelative(2f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                reflectiveCurveToRelative(1f, -0.45f, 1f, -1f)
                verticalLineTo(2f)
                curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
                close()
                moveTo(11f, 20f)
                verticalLineToRelative(2f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                reflectiveCurveToRelative(1f, -0.45f, 1f, -1f)
                verticalLineToRelative(-2f)
                curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f)
                close()
            }
        }.build()
    }

    val Moon: ImageVector by lazy {
        ImageVector.Builder(
            name = "Moon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12.3f, 2f)
                curveToRelative(-0.41f, 0f, -0.73f, 0.37f, -0.66f, 0.77f)
                curveTo(12.44f, 7.39f, 9.6f, 12f, 4.79f, 12.87f)
                curveToRelative(-0.4f, 0.07f, -0.65f, 0.46f, -0.55f, 0.85f)
                curveTo(5.27f, 17.84f, 9.17f, 21f, 13.8f, 21f)
                curveToRelative(5.52f, 0f, 10f, -4.48f, 10f, -10f)
                curveToRelative(0f, -4.68f, -3.22f, -8.6f, -7.58f, -9.62f)
                curveTo(15.86f, 1.3f, 14.16f, 2f, 12.3f, 2f)
                close()
            }
        }.build()
    }

    val EarbudLeft: ImageVector by lazy {
        ImageVector.Builder(
            name = "EarbudLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(13f, 4f)
                curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
                curveToRelative(0f, 1.63f, 0.78f, 3.08f, 2f, 4f)
                verticalLineToRelative(6f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                verticalLineToRelative(-6f)
                curveToRelative(1.22f, -0.92f, 2f, -2.37f, 2f, -4f)
                curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f)
                close()
                moveTo(7f, 8f)
                curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
                reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
                horizontalLineToRelative(1.05f)
                curveTo(8.02f, 9.67f, 8f, 9.34f, 8f, 9f)
                curveToRelative(0f, -0.34f, 0.02f, -0.67f, 0.05f, -1f)
                horizontalLineTo(7f)
                close()
            }
        }.build()
    }

    val EarbudRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "EarbudRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(11f, 4f)
                curveToRelative(2.76f, 0f, 5f, 2.24f, 5f, 5f)
                curveToRelative(0f, 1.63f, -0.78f, 3.08f, -2f, 4f)
                verticalLineToRelative(6f)
                curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
                reflectiveCurveToRelative(-2f, -0.9f, -2f, -2f)
                verticalLineToRelative(-6f)
                curveToRelative(-1.22f, -0.92f, -2f, -2.37f, -2f, -4f)
                curveToRelative(0f, -2.76f, 2.24f, -5f, 5f, -5f)
                close()
                moveTo(17f, 8f)
                curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
                reflectiveCurveToRelative(-0.45f, 1f, -1f, 1f)
                horizontalLineToRelative(-1.05f)
                curveToRelative(0.03f, -0.33f, 0.05f, -0.66f, 0.05f, -1f)
                curveToRelative(0f, -0.34f, -0.02f, -0.67f, -0.05f, -1f)
                horizontalLineTo(17f)
                close()
            }
        }.build()
    }

    val ChargingCase: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChargingCase",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(6f, 5f)
                horizontalLineToRelative(12f)
                curveToRelative(2.21f, 0f, 4f, 1.79f, 4f, 4f)
                verticalLineToRelative(7f)
                curveToRelative(0f, 2.76f, -2.24f, 5f, -5f, 5f)
                horizontalLineTo(7f)
                curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
                verticalLineTo(9f)
                curveToRelative(0f, -2.21f, 1.79f, -4f, 4f, -4f)
                close()
                moveTo(4f, 10f)
                verticalLineToRelative(6f)
                curveToRelative(0f, 1.66f, 1.34f, 3f, 3f, 3f)
                horizontalLineToRelative(10f)
                curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f)
                verticalLineToRelative(-6f)
                horizontalLineTo(4f)
                close()
                moveTo(18f, 7f)
                horizontalLineTo(6f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                horizontalLineToRelative(16f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
            }
        }.build()
    }

    val Eclipse: ImageVector by lazy {
        ImageVector.Builder(
            name = "Eclipse",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
                reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
                reflectiveCurveTo(17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 20f)
                verticalLineTo(4f)
                curveToRelative(4.41f, 0f, 8f, 3.59f, 8f, 8f)
                reflectiveCurveToRelative(-3.59f, 8f, -8f, 8f)
                close()
            }
        }.build()
    }
}
