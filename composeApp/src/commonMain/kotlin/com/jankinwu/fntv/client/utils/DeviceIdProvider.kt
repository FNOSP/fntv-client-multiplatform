package com.jankinwu.fntv.client.utils

/**
 * Represents a device identifier and its source type.
 */
data class DeviceId(
    val id: String,
    val type: String
)

expect fun getDeviceId(context: Context): DeviceId
