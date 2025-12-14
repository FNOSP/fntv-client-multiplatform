package com.jankinwu.fntv.client.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import com.jthemedetecor.OsThemeDetector
import java.util.function.Consumer

@Composable
actual fun isSystemInDarkMode(): Boolean {
    val isSystemInDarkTheme = isSystemInDarkTheme().let { currentValue ->
        remember(currentValue) { mutableStateOf(currentValue) }
    }
    DisposableEffect(isSystemInDarkTheme) {
        val listener = Consumer<Boolean> {
            isSystemInDarkTheme.value = it
        }
        var detector: OsThemeDetector? = null
        try {
            detector = OsThemeDetector.getDetector()
            detector.registerListener(listener)
        } catch (e: Exception) {
            Logger.withTag("DarkThemeMode").e("Failed to register dark theme listener", e)
        }
        onDispose {
            detector?.removeListener(listener)
        }
    }
    return isSystemInDarkTheme.value
}