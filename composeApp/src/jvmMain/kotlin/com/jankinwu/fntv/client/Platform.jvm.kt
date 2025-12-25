/*
 * Copyright (C) 2024-2025 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */
/*
 * Copyright (C) 2025 FNOSP and contributors.
 *
 * This file has been modified from its original version.
 * The modifications are also licensed under the AGPLv3.
 */
package com.jankinwu.fntv.client

import com.jankinwu.fntv.client.bridge.FlutterPlayerBridge
import java.util.Locale

actual fun launchExternalPlayer(url: String, title: String, startPos: Long) {
    FlutterPlayerBridge.launchExternalPlayer(url, title, startPos)
}

internal actual fun currentPlatformImpl(): Platform {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    val archStr = System.getProperty("os.arch").lowercase(Locale.getDefault())

    val arch = when {
        archStr.contains("aarch64") || archStr.contains("arm64") -> Arch.AARCH64
        archStr.contains("arm") -> Arch.ARMV7A // Simplified, can be refined if needed
        else -> Arch.X86_64
    }

    return when {
        os.contains("win") -> Platform.Windows(arch)
        os.contains("mac") -> Platform.MacOS(arch)
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> Platform.Linux(arch)
        else -> error("Unsupported desktop platform: $os")
    }
}

fun currentPlatformDesktop(): Platform.Desktop {
    val platform = currentPlatform()
    if (platform !is Platform.Desktop) {
        error("Current platform is not a desktop platform: $platform")
    }
    return platform
}
