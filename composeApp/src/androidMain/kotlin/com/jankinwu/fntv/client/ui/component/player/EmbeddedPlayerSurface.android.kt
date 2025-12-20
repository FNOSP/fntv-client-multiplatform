package com.jankinwu.fntv.client.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openani.mediamp.MediampPlayer

@Composable
actual fun EmbeddedPlayerSurface(
    mediampPlayer: MediampPlayer,
    modifier: Modifier,
    content: @Composable (() -> Unit)
) {
}