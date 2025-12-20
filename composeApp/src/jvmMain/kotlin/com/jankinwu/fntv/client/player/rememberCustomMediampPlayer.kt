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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import org.openani.mediamp.MediampPlayer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun rememberCustomMediampPlayer(
    parentCoroutineContext: () -> CoroutineContext = { EmptyCoroutineContext },
    mode: CustomVlcMediampPlayer.VlcRenderMode = CustomVlcMediampPlayer.VlcRenderMode.CALLBACK
): MediampPlayer {
    return remember(mode) {
        RememberedCustomMediampPlayer(CustomVlcMediampPlayer(parentCoroutineContext(), mode))
    }.player
}

@Stable
internal class RememberedCustomMediampPlayer(val player: MediampPlayer) : RememberObserver {
    override fun onAbandoned() {
        player.close()
    }

    override fun onForgotten() {
        player.close()
    }

    override fun onRemembered() {
    }
}
