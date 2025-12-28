package com.jankinwu.fntv.client.data.store

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

object PlayingSettingsStore {
    private val settings: Settings = Settings()

    private fun scopedKey(rawKey: String): String {
        val guid = UserInfoMemoryCache.guid
        return if (guid.isNullOrBlank()) rawKey else "$guid::$rawKey"
    }

    data class VideoQuality(val resolution: String, val bitrate: Int?)

    data class PipWindowData(val x: Int, val y: Int, val width: Int, val height: Int)

    fun savePipWindowData(x: Int, y: Int, width: Int, height: Int) {
        settings[scopedKey("pip_window_x")] = x
        settings[scopedKey("pip_window_y")] = y
        settings[scopedKey("pip_window_width")] = width
        settings[scopedKey("pip_window_height")] = height
    }

    fun getPipWindowData(): PipWindowData? {
        val x = settings.getIntOrNull(scopedKey("pip_window_x")) ?: return null
        val y = settings.getIntOrNull(scopedKey("pip_window_y")) ?: return null
        val width = settings.getIntOrNull(scopedKey("pip_window_width")) ?: return null
        val height = settings.getIntOrNull(scopedKey("pip_window_height")) ?: return null
        return PipWindowData(x, y, width, height)
    }

    fun saveQuality(resolution: String, bitrate: Int?) {
        settings[scopedKey("quality_resolution")] = resolution
        if (bitrate != null) {
            settings[scopedKey("quality_bitrate")] = bitrate
        } else {
            settings.remove(scopedKey("quality_bitrate"))
        }
    }

    fun getQuality(): VideoQuality? {
        val resolution = settings.getStringOrNull(scopedKey("quality_resolution")) ?: return null
        val bitrate = settings.getIntOrNull(scopedKey("quality_bitrate"))
        return VideoQuality(resolution, bitrate)
    }

    fun saveVolume(volume: Float) {
        settings[scopedKey("player_volume")] = volume
    }

    fun getVolume(): Float {
        return settings.getFloat(scopedKey("player_volume"), 1.0f)
    }
}
