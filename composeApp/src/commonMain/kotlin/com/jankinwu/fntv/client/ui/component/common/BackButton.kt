package com.jankinwu.fntv.client.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.jankinwu.fntv.client.icons.ArrowLeft
import io.github.composefluent.component.Icon

@Composable
fun BackButton(
    navigator: ComponentNavigator,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { navigator.navigateUp() },
            modifier = Modifier
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.01f),
                            Color.Transparent
                        ),
                        center = Offset(24f, 24f),
                        radius = 30f
                    ),
                    shape = CircleShape
                )
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = ArrowLeft,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}