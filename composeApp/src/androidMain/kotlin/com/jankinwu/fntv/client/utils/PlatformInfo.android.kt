package com.jankinwu.fntv.client.utils

import android.os.Build

actual object PlatformInfo {
    actual val osName: String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    actual val osArch: String = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
}
