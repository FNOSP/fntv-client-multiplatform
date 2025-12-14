package com.jankinwu.fntv.client.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PreRelease: ImageVector
    get() {
        if (_PreRelease != null) {
            return _PreRelease!!
        }
        _PreRelease = ImageVector.Builder(
            name = "PreRelease",
            defaultWidth = 200.dp,
            defaultHeight = 200.dp,
            viewportWidth = 1181f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(38.1f, 0f)
                arcTo(38.8f, 38.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 39.4f)
                curveToRelative(0f, 21.7f, 17f, 39.4f, 38f, 39.4f)
                horizontalLineToRelative(1026.7f)
                arcTo(38.8f, 38.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1102.8f, 39.4f)
                curveToRelative(0f, -21.7f, -17f, -39.4f, -38f, -39.4f)
                lineTo(38.1f, 0f)
                close()
                moveTo(39.4f, 315.1f)
                arcToRelative(39.4f, 39.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 78.8f)
                horizontalLineToRelative(236.3f)
                arcToRelative(39.4f, 39.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -78.8f)
                horizontalLineToRelative(-236.3f)
                close()
                moveTo(39.4f, 630.2f)
                arcToRelative(39.4f, 39.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 78.8f)
                horizontalLineToRelative(466.2f)
                arcToRelative(39.4f, 39.4f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -78.8f)
                lineTo(39.4f, 630.2f)
                close()
                moveTo(38.1f, 945.2f)
                arcToRelative(38.8f, 38.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, -38f, 39.4f)
                curveToRelative(0f, 21.7f, 17f, 39.4f, 38f, 39.4f)
                horizontalLineToRelative(1026.7f)
                arcToRelative(38.8f, 38.8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 38f, -39.4f)
                curveToRelative(0f, -21.7f, -17f, -39.4f, -38f, -39.4f)
                lineTo(38.1f, 945.2f)
                close()
                moveTo(771.4f, 545.9f)
                lineToRelative(100.6f, -93.7f)
                arcToRelative(39.4f, 39.4f, 0f, isMoreThanHalf = true, isPositiveArc = false, -53.7f, -57.5f)
                lineTo(720.2f, 485.8f)
                lineTo(509.2f, 406.1f)
                lineToRelative(528.6f, -184.2f)
                lineToRelative(-190.3f, 529.6f)
                lineToRelative(-76.2f, -205.6f)
                close()
                moveTo(692.4f, 559.6f)
                lineToRelative(98.5f, 265.8f)
                arcToRelative(60.7f, 60.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, 113.9f, -0.5f)
                lineToRelative(220f, -612.1f)
                arcToRelative(60.7f, 60.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -77f, -77.8f)
                lineTo(436.2f, 348.2f)
                arcToRelative(60.7f, 60.7f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.4f, 114f)
                lineTo(692.4f, 559.6f)
                close()
            }
        }.build()

        return _PreRelease!!
    }

@Suppress("ObjectPropertyName")
private var _PreRelease: ImageVector? = null