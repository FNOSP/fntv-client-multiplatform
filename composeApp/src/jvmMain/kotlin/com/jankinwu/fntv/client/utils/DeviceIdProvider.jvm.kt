package com.jankinwu.fntv.client.utils

import oshi.SystemInfo

actual fun getDeviceId(context: Context): String {
    return try {
        val systemInfo = SystemInfo()
        val hardware = systemInfo.hardware
        val computerSystem = hardware.computerSystem
        // Try to get serial number, if not available use hardware UUID, then fallback to baseboard serial
        computerSystem.serialNumber.takeIf { it.isNotBlank() && it != "unknown" }
            ?: computerSystem.hardwareUUID.takeIf { it.isNotBlank() && it != "unknown" }
            ?: computerSystem.baseboard.serialNumber.takeIf { it.isNotBlank() && it != "unknown" }
            ?: "unknown_jvm"
    } catch (e: Exception) {
        "error_jvm"
    }
}