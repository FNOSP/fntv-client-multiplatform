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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.openani.mediamp.InternalMediampApi
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.swing.SwingUtilities

@OptIn(InternalMediampApi::class)
public class CustomSkiaBitmapVideoSurface(
    private val adapter: VideoSurfaceAdapter = VideoSurfaceAdapters.getVideoSurfaceAdapter()
) : VideoSurface(adapter) {
    private val videoSurface = SkiaVideoSurface()

    @Volatile
    private lateinit var imageInfo: ImageInfo

    @Volatile
    private lateinit var frameBytes: ByteArray
    private val skiaBitmap: Bitmap = Bitmap()
    private val composeBitmap = mutableStateOf<ImageBitmap?>(null)

    public val enableRendering: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Set this to non-zero to draw frames even if [enableRendering] is true.
     *
     * @see ALLOWED_DRAW_FRAMES
     */
    @JvmField
    @Volatile
    public var allowedDrawFrames: Int = 0

    public fun setAllowedDrawFrames(value: Int) {
        ALLOWED_DRAW_FRAMES.set(this, value)
    }

    public val bitmap: ImageBitmap? by composeBitmap

    public fun clearBitmap() {
        composeBitmap.value = null
    }

    override fun attach(mediaPlayer: MediaPlayer) {
        videoSurface.attach(mediaPlayer)
    }

    private inner class SkiaBitmapBufferFormatCallback : BufferFormatCallback {
        private var sourceWidth: Int = 0
        private var sourceHeight: Int = 0

        override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
            this.sourceWidth = sourceWidth
            this.sourceHeight = sourceHeight
            return RV32BufferFormat(sourceWidth, sourceHeight)
        }

        override fun allocatedBuffers(buffers: Array<ByteBuffer>) {
            frameBytes = buffers[0].run { ByteArray(remaining()).also(::get) }
            imageInfo = ImageInfo(
                sourceWidth,
                sourceHeight,
                ColorType.BGRA_8888,
                ColorAlphaType.PREMUL,
            )
        }
    }

    private inner class SkiaBitmapRenderCallback : RenderCallback {
        override fun display(
            mediaPlayer: MediaPlayer,
            nativeBuffers: Array<ByteBuffer>,
            bufferFormat: BufferFormat,
        ) {
            val allowedDrawFramesValue = ALLOWED_DRAW_FRAMES.get(this@CustomSkiaBitmapVideoSurface)

            if (!enableRendering.value) {
                if (allowedDrawFramesValue <= 0) {
                    return
                }
                if (ALLOWED_DRAW_FRAMES.decrementAndGet(this@CustomSkiaBitmapVideoSurface) < 0) return
            } else {
                // 允许渲染, 不考虑 allowedDrawFrames
            }

            SwingUtilities.invokeLater {
                nativeBuffers[0].rewind()
                nativeBuffers[0].get(frameBytes)
                skiaBitmap.installPixels(imageInfo, frameBytes, bufferFormat.width * 4)
                composeBitmap.value = skiaBitmap.asComposeImageBitmap()
            }
        }
    }

    private inner class SkiaVideoSurface : CallbackVideoSurface(
        SkiaBitmapBufferFormatCallback(),
        SkiaBitmapRenderCallback(),
        true,
        adapter,
    )

    private companion object {
        private val ALLOWED_DRAW_FRAMES = AtomicIntegerFieldUpdater.newUpdater(
            CustomSkiaBitmapVideoSurface::class.java,
            "allowedDrawFrames",
        )
    }
}
