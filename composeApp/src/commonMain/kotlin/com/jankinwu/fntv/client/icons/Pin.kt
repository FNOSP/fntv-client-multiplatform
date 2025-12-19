package com.jankinwu.fntv.client.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Pin: ImageVector
    get() {
        if (_Pin != null) {
            return _Pin!!
        }
        _Pin = ImageVector.Builder(
            name = "Pin",
            defaultWidth = 200.dp,
            defaultHeight = 200.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(648.7f, 130.8f)
                arcToRelative(73.1f, 73.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22.7f, 15.4f)
                lineToRelative(191.6f, 191.8f)
                arcToRelative(73.1f, 73.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -22.1f, 118.6f)
                lineToRelative(-67.9f, 30.1f)
                lineToRelative(-127.3f, 127.5f)
                lineToRelative(-10.1f, 140.2f)
                arcToRelative(73.1f, 73.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, -124.7f, 46.4f)
                lineToRelative(-123.7f, -123.8f)
                lineToRelative(-210.7f, 211.7f)
                lineToRelative(-51.8f, -51.6f)
                lineToRelative(210.8f, -211.8f)
                lineToRelative(-127.9f, -128f)
                arcToRelative(73.1f, 73.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 46.3f, -124.6f)
                lineToRelative(144.2f, -10.8f)
                lineToRelative(125.1f, -125.2f)
                lineToRelative(29.4f, -67.8f)
                arcToRelative(73.1f, 73.1f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96.2f, -38f)
                close()
                moveTo(619.6f, 197.9f)
                lineToRelative(-34.9f, 80.5f)
                lineToRelative(-154.1f, 154.3f)
                lineToRelative(-171.4f, 12.8f)
                lineToRelative(303.3f, 303.5f)
                lineToRelative(12f, -167.4f)
                lineToRelative(156.2f, -156.4f)
                lineToRelative(80.4f, -35.6f)
                lineToRelative(-191.6f, -191.7f)
                close()
            }
        }.build()

        return _Pin!!
    }

@Suppress("ObjectPropertyName")
private var _Pin: ImageVector? = null
