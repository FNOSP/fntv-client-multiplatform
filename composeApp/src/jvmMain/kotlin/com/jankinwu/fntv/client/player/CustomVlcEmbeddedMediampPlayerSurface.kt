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
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color

@Composable
public fun CustomVlcEmbeddedMediampPlayerSurface(
    mediampPlayer: CustomVlcMediampPlayer,
    modifier: Modifier = Modifier,
) {
    if (mediampPlayer.mode != CustomVlcMediampPlayer.VlcRenderMode.EMBEDDED) {
        return
    }

    val component = mediampPlayer.component ?: return

    SwingPanel(
        background = Color.Black,
        modifier = modifier,
        factory = {
            component
        }
    )
}
