package com.jankinwu.fntv.client.utils

import android.annotation.SuppressLint
import android.provider.Settings

@SuppressLint("HardwareIds")
actual fun getDeviceId(context: Context): DeviceId {
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    return if (androidId != null) {
        DeviceId(androidId, "android_id")
    } else {
        DeviceId("unknown_android", "unknown")
    }
}
