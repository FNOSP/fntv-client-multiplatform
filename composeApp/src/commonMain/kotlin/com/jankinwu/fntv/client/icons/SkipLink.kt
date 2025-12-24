package com.jankinwu.fntv.client.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SkipLink: ImageVector
    get() {
        if (_SkipLink != null) {
            return _SkipLink!!
        }
        _SkipLink = ImageVector.Builder(
            name = "SkipLink",
            defaultWidth = 200.dp,
            defaultHeight = 200.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(780.7f, 964.8f)
                horizontalLineTo(193.9f)
                arcTo(129.9f, 129.9f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64f, 835f)
                verticalLineTo(248.1f)
                arcToRelative(130f, 130f, 0f, isMoreThanHalf = false, isPositiveArc = true, 129.9f, -129.9f)
                horizontalLineToRelative(224.8f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 88f)
                horizontalLineTo(193.9f)
                arcToRelative(41.9f, 41.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, -41.9f, 41.9f)
                verticalLineToRelative(586.9f)
                curveToRelative(0f, 23.1f, 18.8f, 41.9f, 41.9f, 41.9f)
                horizontalLineToRelative(586.8f)
                arcToRelative(41.9f, 41.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, 41.9f, -41.9f)
                verticalLineTo(596.6f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = true, isPositiveArc = true, 87.9f, 0f)
                verticalLineToRelative(238.5f)
                arcToRelative(129.9f, 129.9f, 0f, isMoreThanHalf = false, isPositiveArc = true, -129.8f, 129.7f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(497f, 554.8f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = false, isPositiveArc = true, -31.1f, -75.1f)
                lineToRelative(384.9f, -384.9f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.2f, 62.2f)
                lineTo(528.1f, 541.9f)
                arcToRelative(43.7f, 43.7f, 0f, isMoreThanHalf = false, isPositiveArc = true, -31.1f, 12.9f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(916f, 412.7f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44f, -44f)
                verticalLineTo(147.1f)
                horizontalLineTo(627.5f)
                arcToRelative(44f, 44f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -87.9f)
                horizontalLineToRelative(267.5f)
                curveToRelative(35.9f, 0f, 65f, 29.1f, 65.1f, 65.1f)
                verticalLineTo(368.6f)
                curveToRelative(0f, 24.3f, -19.7f, 44f, -44f, 44f)
                close()
            }
        }.build()

        return _SkipLink!!
    }

@Suppress("ObjectPropertyName")
private var _SkipLink: ImageVector? = null