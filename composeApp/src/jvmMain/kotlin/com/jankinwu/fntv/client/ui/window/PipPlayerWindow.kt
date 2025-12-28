package com.jankinwu.fntv.client.ui.window

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.jankinwu.fntv.client.data.store.PlayingSettingsStore
import com.jankinwu.fntv.client.manager.PlayerResourceManager
import com.jankinwu.fntv.client.ui.providable.LocalMediaPlayer
import com.jankinwu.fntv.client.ui.providable.LocalPlayerManager
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.flow.debounce
import org.openani.mediamp.PlaybackState
import org.openani.mediamp.compose.MediampPlayerSurface

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PipPlayerWindow(
    onClose: () -> Unit,
    onExitPip: () -> Unit
) {
    val playerManager = LocalPlayerManager.current
    val mediaPlayer = LocalMediaPlayer.current
    val savedData = remember { PlayingSettingsStore.getPipWindowData() }

    val windowState = rememberWindowState(
        position = if (savedData != null) WindowPosition(savedData.x.dp, savedData.y.dp) else WindowPosition.Aligned(Alignment.BottomEnd),
        width = if (savedData != null) savedData.width.dp else 320.dp,
        height = if (savedData != null) savedData.height.dp else 180.dp
    )

    // Auto-save position
    LaunchedEffect(windowState) {
        snapshotFlow { windowState.position to windowState.size }
            .debounce(500)
            .collect { (pos, size) ->
                if (pos is WindowPosition.Absolute) {
                    PlayingSettingsStore.savePipWindowData(
                        pos.x.value.toInt(),
                        pos.y.value.toInt(),
                        size.width.value.toInt(),
                        size.height.value.toInt()
                    )
                }
            }
    }

    Window(
        onCloseRequest = onClose,
        state = windowState,
        undecorated = true,
        alwaysOnTop = true,
        resizable = true,
        transparent = false,
        title = "PiP Player"
    ) {
        var isHovered by remember { mutableStateOf(false) }
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                .onPointerEvent(PointerEventType.Exit) { isHovered = false }
        ) {
            // Video Surface
            MediampPlayerSurface(
                mediampPlayer = mediaPlayer,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (mediaPlayer.getCurrentPlaybackState() == PlaybackState.PLAYING) {
                                    mediaPlayer.pause()
                                } else {
                                    mediaPlayer.resume()
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val pos = windowState.position
                            if (pos is WindowPosition.Absolute) {
                                with(density) {
                                    windowState.position = WindowPosition(
                                        pos.x + dragAmount.x.toDp(),
                                        pos.y + dragAmount.y.toDp()
                                    )
                                }
                            }
                        }
                    }
            )

            if (isHovered) {
                // Top Right: Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Bottom Right: Exit PiP Button
                val pipSpec = PlayerResourceManager.quitPipSpec
                if (pipSpec != null) {
                    val composition by rememberLottieComposition { pipSpec }
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = 1,
                        isPlaying = true
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clickable { onExitPip() }
                    ) {
                        Image(
                            painter = rememberLottiePainter(composition, progress = { progress }),
                            contentDescription = "Exit PiP",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
