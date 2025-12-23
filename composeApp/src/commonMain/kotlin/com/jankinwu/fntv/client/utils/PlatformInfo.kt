package com.jankinwu.fntv.client.utils

/**
 * Platform-specific information.
 */
expect object PlatformInfo {
    val osName: String
    val osArch: String
}
