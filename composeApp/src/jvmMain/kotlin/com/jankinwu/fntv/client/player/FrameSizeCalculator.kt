/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * Use of this source code is governed by the GNU GENERAL PUBLIC LICENSE version 3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/mediamp/blob/main/LICENSE
 */
/*
 * Copyright (C) 2025 FNOSP and contributors.
 *
 * This file has been modified from its original version.
 * The modifications are also licensed under the AGPLv3.
 */

package com.jankinwu.fntv.client.player

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.openani.mediamp.features.AspectRatioMode
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal class FrameSizeCalculator {
    private var lastImageSize: IntSize = IntSize.Zero
    private var lastFrameSizePx: IntSize = IntSize.Zero
    private var lastAspectRatioMode: AspectRatioMode = AspectRatioMode.FIT

    var dstSize: IntSize = IntSize.Zero
        private set
    var dstOffset: IntOffset = IntOffset.Zero
        private set

    /**
     * Calculate size and offset for FIT mode - maintain aspect ratio, fit entirely within container.
     */
    private fun calculateFitInside(
        imageSize: IntSize,
        frameSize: Size
    ) {
        val scale = min(
            frameSize.width / imageSize.width,
            frameSize.height / imageSize.height,
        )

        val scaledW = (imageSize.width * scale).roundToInt()
        val scaledH = (imageSize.height * scale).roundToInt()

        dstSize = IntSize(scaledW, scaledH)

        val offsetX = ((frameSize.width - scaledW) / 2f).coerceAtLeast(0f).roundToInt()
        val offsetY = ((frameSize.height - scaledH) / 2f).coerceAtLeast(0f).roundToInt()
        dstOffset = IntOffset(offsetX, offsetY)
    }

    /**
     * Calculate size and offset for STRETCH mode - fill entire container, may change aspect ratio.
     */
    private fun calculateStretch(
        frameSize: Size
    ) {
        dstSize = IntSize(frameSize.width.roundToInt(), frameSize.height.roundToInt())
        dstOffset = IntOffset.Zero
    }

    /**
     * Calculate size and offset for FILL mode - maintain aspect ratio, fill entire container.
     */
    private fun calculateFill(
        imageSize: IntSize,
        frameSize: Size
    ) {
        val scale = max(
            frameSize.width / imageSize.width,
            frameSize.height / imageSize.height,
        )

        val scaledW = (imageSize.width * scale).roundToInt()
        val scaledH = (imageSize.height * scale).roundToInt()

        dstSize = IntSize(scaledW, scaledH)

        val offsetX = ((frameSize.width - scaledW) / 2f).roundToInt()
        val offsetY = ((frameSize.height - scaledH) / 2f).roundToInt()
        dstOffset = IntOffset(offsetX, offsetY)
    }

    fun calculate(imageSize: IntSize, frameSize: Size, aspectRatioMode: AspectRatioMode) {
        val frameSizePx = IntSize(frameSize.width.roundToInt(), frameSize.height.roundToInt())

        if (lastImageSize == imageSize && lastFrameSizePx == frameSizePx && lastAspectRatioMode == aspectRatioMode) return

        when (aspectRatioMode) {
            AspectRatioMode.FIT -> calculateFitInside(imageSize, frameSize)
            AspectRatioMode.STRETCH -> calculateStretch(frameSize)
            AspectRatioMode.CROP -> calculateFill(imageSize, frameSize)
        }

        lastImageSize = imageSize
        lastFrameSizePx = frameSizePx
        lastAspectRatioMode = aspectRatioMode
    }
}

// Helper extension method to avoid dependency on internal libraries if any
private fun Float.fastCoerceAtLeast(minimumValue: Float): Float {
    return if (this < minimumValue) minimumValue else this
}
