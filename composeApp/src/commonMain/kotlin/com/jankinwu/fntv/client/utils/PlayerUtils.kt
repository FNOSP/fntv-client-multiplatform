package com.jankinwu.fntv.client.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.isActive
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.PlaybackState
import kotlin.math.abs

@Composable
fun rememberSmoothVideoTime(mediaPlayer: MediampPlayer): State<Long> {
    val targetTime by mediaPlayer.currentPositionMillis.collectAsState()
    val isPlaying by mediaPlayer.playbackState.collectAsState()
    val smoothTime = remember { mutableLongStateOf(targetTime) }

    // Sync when paused or seeking (large diff)
    LaunchedEffect(targetTime, isPlaying) {
        if (isPlaying != PlaybackState.PLAYING || abs(smoothTime.longValue - targetTime) > 1000) {
            smoothTime.longValue = targetTime
        }
    }

    // Smooth update loop
    LaunchedEffect(isPlaying) {
        if (isPlaying == PlaybackState.PLAYING) {
            var lastFrameTime = withFrameNanos { it }
            while (isActive) {
                withFrameNanos { frameTime ->
                    val delta = (frameTime - lastFrameTime) / 1_000_000 // ns to ms
                    if (delta > 0) {
                        smoothTime.longValue += delta
                    }
                    lastFrameTime = frameTime
                }
            }
        }
    }
    return smoothTime
}
