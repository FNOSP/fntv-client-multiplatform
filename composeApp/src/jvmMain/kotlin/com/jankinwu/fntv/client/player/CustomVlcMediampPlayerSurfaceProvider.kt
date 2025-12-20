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
import androidx.compose.ui.Modifier
import org.openani.mediamp.compose.MediampPlayerSurfaceProvider
import kotlin.reflect.KClass

public class CustomVlcMediampPlayerSurfaceProvider : MediampPlayerSurfaceProvider<CustomVlcMediampPlayer> {
    override val forClass: KClass<CustomVlcMediampPlayer> = CustomVlcMediampPlayer::class

    @Composable
    override fun Surface(mediampPlayer: CustomVlcMediampPlayer, modifier: Modifier) {
        if (mediampPlayer.mode == CustomVlcMediampPlayer.VlcRenderMode.EMBEDDED) {
            CustomVlcEmbeddedMediampPlayerSurface(mediampPlayer, modifier)
        } else {
            CustomVlcMediampPlayerSurface(mediampPlayer, modifier)
        }
    }
}
