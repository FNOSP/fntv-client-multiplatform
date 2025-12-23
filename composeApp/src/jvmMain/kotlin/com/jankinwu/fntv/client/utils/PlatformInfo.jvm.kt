package com.jankinwu.fntv.client.utils

import java.util.Locale

actual object PlatformInfo {
    actual val osName: String = System.getProperty("os.name") ?: "Unknown JVM"
    actual val osArch: String = System.getProperty("os.arch")?.lowercase(Locale.getDefault()) ?: "unknown"
}
