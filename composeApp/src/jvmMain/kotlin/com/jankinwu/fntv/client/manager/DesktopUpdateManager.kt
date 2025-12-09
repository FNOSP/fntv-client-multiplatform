package com.jankinwu.fntv.client.manager

import co.touchlab.kermit.Logger
import com.jankinwu.fntv.client.BuildConfig
import com.jankinwu.fntv.client.utils.ExecutableDirectoryDetector
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.serialization.jackson.jackson
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.fasterxml.jackson.databind.DeserializationFeature
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.max

class DesktopUpdateManager : UpdateManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    override val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    override fun checkUpdate(proxyUrl: String) {
        scope.launch {
            _status.value = UpdateStatus.Checking
            try {
                val baseUrl = if (proxyUrl.isNotBlank()) {
                    if (proxyUrl.endsWith("/")) proxyUrl else "$proxyUrl/"
                } else {
                    ""
                }
                
                val targetUrl = "https://api.github.com/repos/FNOSP/fntv-client-multiplatform/releases/latest"

                Logger.i("Checking update from: $targetUrl")
                
                val release: GitHubRelease = client.get(targetUrl).body()
                val currentVersion = BuildConfig.VERSION_NAME
                
                val remoteVersion = release.name.removePrefix("v").trim()
                
                Logger.i("Current version: $currentVersion, Remote version: $remoteVersion")

                if (compareVersions(remoteVersion, currentVersion) > 0) {
                    val arch = getSystemArch()
                    // Naming convention: FnMedia_Setup_{Arch}_{Version}.{Ext}
                    val asset = release.assets.find { 
                        it.name.contains(arch, ignoreCase = true) && 
                        it.name.endsWith(".exe", ignoreCase = true) 
                    }
                    
                    if (asset != null) {
                         _status.value = UpdateStatus.Available(
                             UpdateInfo(
                                 version = remoteVersion,
                                 releaseNotes = release.body,
                                 downloadUrl = asset.browserDownloadUrl,
                                 fileName = asset.name,
                                 size = asset.size
                             )
                         )
                    } else {
                        _status.value = UpdateStatus.Error("No compatible asset found for arch: $arch")
                    }
                } else {
                    _status.value = UpdateStatus.UpToDate
                }
            } catch (e: Exception) {
                Logger.e("Update check failed", e)
                _status.value = UpdateStatus.Error("Update check failed: ${e.message}")
            }
        }
    }

    override fun downloadUpdate(proxyUrl: String, info: UpdateInfo) {
        scope.launch {
            _status.value = UpdateStatus.Downloading(0f)
            try {
                val baseUrl = if (proxyUrl.isNotBlank()) {
                    if (proxyUrl.endsWith("/")) proxyUrl else "$proxyUrl/"
                } else {
                    ""
                }
                val url = if (baseUrl.isNotBlank()) {
                    "$baseUrl${info.downloadUrl}"
                } else {
                    info.downloadUrl
                }
                
                Logger.i("Downloading update from: $url")

                val executableDir = ExecutableDirectoryDetector.INSTANCE.getExecutableDirectory()
                val file = File(executableDir, info.fileName)

                client.prepareGet(url).execute { httpResponse ->
                    val channel = httpResponse.bodyAsChannel()
                    val totalSize = httpResponse.contentLength() ?: info.size
                    
                    var downloadedSize = 0L
                    val buffer = ByteArray(1024 * 8)
                    val output = FileOutputStream(file)
                    
                    try {
                        while (!channel.isClosedForRead) {
                            val read = channel.readAvailable(buffer, 0, buffer.size)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            downloadedSize += read
                            if (totalSize > 0) {
                                _status.value = UpdateStatus.Downloading(downloadedSize.toFloat() / totalSize)
                            }
                        }
                    } finally {
                        output.close()
                    }
                }
                _status.value = UpdateStatus.Downloaded(file.absolutePath)
            } catch (e: Exception) {
                 Logger.e("Download failed", e)
                _status.value = UpdateStatus.Error("Download failed: ${e.message}")
            }
        }
    }
    
    override fun clearStatus() {
        _status.value = UpdateStatus.Idle
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }
        val length = max(parts1.size, parts2.size)
        
        for (i in 0 until length) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }

    private fun getSystemArch(): String {
        val osArch = System.getProperty("os.arch").lowercase(Locale.getDefault())
        return when {
            osArch.contains("aarch64") || osArch.contains("arm64") -> "arm64"
            osArch.contains("amd64") || osArch.contains("x86_64") -> "amd64"
            else -> "x86" 
        }
    }
}
