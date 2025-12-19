package com.jankinwu.fntv.client.data.model

data class SubtitleSettings(
    val offsetSeconds: Float = 0f,
    val verticalPosition: Float = 0.1f, // 0.0 (Bottom) to 1.0 (Top). Default slightly up from bottom.
    val fontScale: Float = 1.0f
)