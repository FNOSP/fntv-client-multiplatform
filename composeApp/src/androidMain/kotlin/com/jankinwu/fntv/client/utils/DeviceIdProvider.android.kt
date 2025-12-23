package com.jankinwu.fntv.client.utils

import android.annotation.SuppressLint
import android.provider.Settings

@SuppressLint("HardwareIds")
actual fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_android"
}
