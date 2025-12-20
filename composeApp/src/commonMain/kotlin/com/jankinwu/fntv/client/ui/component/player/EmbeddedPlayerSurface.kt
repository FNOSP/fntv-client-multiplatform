package com.jankinwu.fntv.client.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openani.mediamp.MediampPlayer

@Composable
expect fun EmbeddedPlayerSurface(
    mediampPlayer: MediampPlayer,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
