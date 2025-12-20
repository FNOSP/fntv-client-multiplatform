package com.jankinwu.fntv.client.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import com.jankinwu.fntv.client.player.CustomVlcMediampPlayer
import com.jankinwu.fntv.client.ui.providable.LocalFileInfo
import com.jankinwu.fntv.client.ui.providable.LocalFrameWindowScope
import com.jankinwu.fntv.client.ui.providable.LocalIsoTagData
import com.jankinwu.fntv.client.ui.providable.LocalMediaPlayer
import com.jankinwu.fntv.client.ui.providable.LocalPlayerManager
import com.jankinwu.fntv.client.ui.providable.LocalStore
import com.jankinwu.fntv.client.ui.providable.LocalToastManager
import com.jankinwu.fntv.client.ui.providable.LocalTypography
import com.jankinwu.fntv.client.ui.providable.LocalWindowState
import io.github.composefluent.FluentTheme
import org.openani.mediamp.MediampPlayer
import javax.swing.JLayeredPane
import javax.swing.OverlayLayout

@Composable
actual fun EmbeddedPlayerSurface(
    mediampPlayer: MediampPlayer,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    // Only support CustomVlcMediampPlayer in Embedded mode
    if (mediampPlayer !is CustomVlcMediampPlayer || mediampPlayer.mode != CustomVlcMediampPlayer.VlcRenderMode.EMBEDDED) {
        return
    }

    val vlcComponent = mediampPlayer.component ?: return

    // Capture CompositionLocals to bridge them to the ComposePanel
    val playerManagerState = rememberUpdatedState(LocalPlayerManager.current)
    val mediaPlayerState = rememberUpdatedState(LocalMediaPlayer.current)
    val frameWindowScopeState = rememberUpdatedState(LocalFrameWindowScope.current)
    val windowStateState = rememberUpdatedState(LocalWindowState.current)
    val toastManagerState = rememberUpdatedState(LocalToastManager.current)
    val isoTagDataState = rememberUpdatedState(LocalIsoTagData.current)
    // LocalFileInfo is nullable, use runCatching or check if provided?
    // It is defined as staticCompositionLocalOf { error(...) }.
    // We assume it is provided where EmbeddedPlayerSurface is used.
    val fileInfoState = rememberUpdatedState(LocalFileInfo.current)
    val storeState = rememberUpdatedState(LocalStore.current)
    val typographyState = rememberUpdatedState(LocalTypography.current)

    // Capture FluentTheme properties
    val fluentColorsState = rememberUpdatedState(FluentTheme.colors)
    val fluentTypographyState = rememberUpdatedState(FluentTheme.typography)

    val currentContent by rememberUpdatedState(content)

    SwingPanel(
        background = Color.Black,
        modifier = modifier,
        factory = {
            val layeredPane = object : JLayeredPane() {
                override fun doLayout() {
                    // Do not call super.doLayout() as we are handling it manually and layout is null
                    // super.doLayout() 
                    
                    // Manually layout components to fill the pane starting from (0,0)
                    val w = width
                    val h = height
                    for (component in components) {
                        component.setBounds(0, 0, w, h)
                    }
                }
            }
            layeredPane.isOpaque = false
            layeredPane.background = java.awt.Color(0, 0, 0, 0)
            layeredPane.layout = null // Use absolute layout with manual bounds in doLayout

            // Bottom Layer: VLC Video Component
            layeredPane.add(vlcComponent.apply {
                background = java.awt.Color.BLACK
            }, JLayeredPane.DEFAULT_LAYER)

            // Top Layer: Compose UI Overlay
            layeredPane.add(ComposePanel().apply {
                // Transparent background for the panel itself
                background = java.awt.Color(0, 0, 0, 0)
                isOpaque = false

                // Set the Compose content
                setContent {
                    FluentTheme(
                        colors = fluentColorsState.value,
                        typography = fluentTypographyState.value
                    ) {
                        CompositionLocalProvider(
                            LocalPlayerManager provides playerManagerState.value,
                            LocalMediaPlayer provides mediaPlayerState.value,
                            LocalFrameWindowScope provides frameWindowScopeState.value,
                            LocalWindowState provides windowStateState.value,
                            LocalToastManager provides toastManagerState.value,
                            LocalIsoTagData provides isoTagDataState.value,
                            LocalFileInfo provides fileInfoState.value,
                            LocalStore provides storeState.value,
                            LocalTypography provides typographyState.value
                        ) {
                            currentContent()
                        }
                    }
                }
            }, JLayeredPane.PALETTE_LAYER)

            layeredPane
        },
        update = { container ->
            // No update needed for structure, content updates via State objects
        }
    )
}
