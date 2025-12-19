package com.jankinwu.fntv.client.utils

import androidx.compose.ui.text.AnnotatedString

data class SubtitleCue(
    val startTime: Long, // milliseconds
    val endTime: Long,   // milliseconds
    val text: AnnotatedString,
    val assProps: AssProperties? = null
)

data class AssProperties(
    val playResX: Int,
    val playResY: Int,
    val fontSize: Float,
    val alignment: Int = 2, // Default 2 (Bottom Center) in ASS
    val position: AssPosition? = null,
    val move: AssMove? = null,
    val fade: AssFade? = null,
    val rotationZ: Float? = null,
    val alpha: Float? = null, // 0.0 (opaque) to 1.0 (transparent) in ASS usually, but let's store as 0..1 opacity? 
    // Wait, ASS alpha is &H00 (opaque) to &HFF (transparent). Let's store as opacity (1.0 = opaque, 0.0 = transparent) for Compose easier use?
    // Or stick to raw and convert later. Let's use opacity: 1f = visible, 0f = invisible.
    val clip: AssClip? = null
)

data class AssPosition(
    val x: Float,
    val y: Float
)

data class AssMove(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val t1: Long? = null,
    val t2: Long? = null
)

data class AssFade(
    val t1: Long, // fade in duration
    val t2: Long  // fade out duration
)

data class AssClip(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)
