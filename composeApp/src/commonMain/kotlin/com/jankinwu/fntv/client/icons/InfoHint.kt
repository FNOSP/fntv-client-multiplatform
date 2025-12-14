package com.jankinwu.fntv.client.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val InfoHint: ImageVector
    get() {
        if (_infoHint != null) {
            return _infoHint!!
        }
        _infoHint = ImageVector.Builder(
            name = "infoHint",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 23f)
                curveTo(18.075f, 23f, 23f, 18.075f, 23f, 12f)
                curveTo(23f, 5.925f, 18.075f, 1f, 12f, 1f)
                curveTo(5.925f, 1f, 1f, 5.925f, 1f, 12f)
                curveTo(1f, 18.075f, 5.925f, 23f, 12f, 23f)
                close()
                moveTo(14f, 7f)
                curveTo(14f, 8.105f, 13.105f, 9f, 12f, 9f)
                curveTo(10.895f, 9f, 10f, 8.105f, 10f, 7f)
                curveTo(10f, 5.895f, 10.895f, 5f, 12f, 5f)
                curveTo(13.105f, 5f, 14f, 5.895f, 14f, 7f)
                close()
                moveTo(9f, 10.75f)
                curveTo(9f, 10.336f, 9.336f, 10f, 9.75f, 10f)
                horizontalLineTo(12.5f)
                curveTo(13.052f, 10f, 13.5f, 10.448f, 13.5f, 11f)
                verticalLineTo(16.5f)
                horizontalLineTo(14.25f)
                curveTo(14.664f, 16.5f, 15f, 16.836f, 15f, 17.25f)
                curveTo(15f, 17.664f, 14.664f, 18f, 14.25f, 18f)
                horizontalLineTo(9.75f)
                curveTo(9.336f, 18f, 9f, 17.664f, 9f, 17.25f)
                curveTo(9f, 16.836f, 9.336f, 16.5f, 9.75f, 16.5f)
                horizontalLineTo(10.5f)
                verticalLineTo(11.5f)
                horizontalLineTo(9.75f)
                curveTo(9.336f, 11.5f, 9f, 11.164f, 9f, 10.75f)
                close()
            }
        }.build()

        return _infoHint!!
    }

@Suppress("ObjectPropertyName")
private var _infoHint: ImageVector? = null