package com.jankinwu.fntv.client.bridge

import co.touchlab.kermit.Logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jankinwu.fntv.client.Platform
import com.jankinwu.fntv.client.currentPlatform
import com.jankinwu.fntv.client.currentPlatformDesktop
import com.jankinwu.fntv.client.data.network.fnOfficialClient
import com.jankinwu.fntv.client.data.store.AccountDataCache
import com.jankinwu.fntv.client.data.store.AppSettingsStore
import com.jankinwu.fntv.client.data.store.PlayingSettingsStore
import com.jankinwu.fntv.client.utils.ExecutableDirectoryDetector
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

object FlutterPlayerBridge {
    private val logger = Logger.withTag("FlutterPlayerBridge")
    private const val PLAYER_PORT = 47920 // Port Flutter listens on
    private const val BRIDGE_PORT = 47921 // Port KMP listens on
    private var externalPlayerProcess: Process? = null
    private val isServerRunning = AtomicBoolean(false)
    private val mapper = jacksonObjectMapper()

    fun startServer() {
        if (isServerRunning.getAndSet(true)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                embeddedServer(Netty, port = BRIDGE_PORT) {
                    install(ContentNegotiation) {
                        jackson()
                    }
                    routing {
                        get("/proxy-config") {
                            val config = mapOf(
                                "baseUrl" to AccountDataCache.getProxyBaseUrl(),
                                "headers" to mapOf(
                                    "Authorization" to AccountDataCache.authorization
                                )
                            )
                            call.respond(config)
                        }

                        get("/settings/app") {
                            val settings = mapOf(
                                "githubResourceProxyUrl" to AppSettingsStore.githubResourceProxyUrl,
                                "isFollowingSystemTheme" to AppSettingsStore.isFollowingSystemTheme,
                                "darkMode" to AppSettingsStore.darkMode,
                                "autoPlay" to AppSettingsStore.autoPlay,
                                "useExternalPlayer" to AppSettingsStore.useExternalPlayer
                            )
                            call.respond(settings)
                        }

                        post("/settings/app") {
                            val body = call.receiveText()
                            val settings = mapper.readValue(body, Map::class.java) as Map<String, Any>
                            settings["autoPlay"]?.let { AppSettingsStore.autoPlay = it as Boolean }
                            // Add other settings updates as needed
                            call.respond(HttpStatusCode.OK)
                        }
                        
                        get("/settings/player") {
                             val quality = PlayingSettingsStore.getQuality()
                             val settings = mapOf(
                                 "volume" to PlayingSettingsStore.getVolume(),
                                 "quality_resolution" to quality?.resolution,
                                 "quality_bitrate" to quality?.bitrate,
                                 "playerWindowWidth" to AppSettingsStore.playerWindowWidth,
                                 "playerWindowHeight" to AppSettingsStore.playerWindowHeight,
                                 "playerWindowX" to AppSettingsStore.playerWindowX,
                                 "playerWindowY" to AppSettingsStore.playerWindowY,
                                 "playerIsFullscreen" to AppSettingsStore.playerIsFullscreen,
                                 "playerWindowAspectRatio" to AppSettingsStore.playerWindowAspectRatio,
                                 "navigationDisplayMode" to AppSettingsStore.navigationDisplayMode
                             )
                             call.respond(settings)
                        }

                        post("/settings/player") {
                            val body = call.receiveText()
                            val settings = mapper.readValue(body, Map::class.java) as Map<String, Any>
                            
                            settings["volume"]?.let { PlayingSettingsStore.saveVolume((it as Number).toFloat()) }
                            
                            val resolution = settings["quality_resolution"] as? String
                            val bitrate = settings["quality_bitrate"] as? Int
                            if (resolution != null) {
                                PlayingSettingsStore.saveQuality(resolution, bitrate)
                            }
                            
                            settings["playerWindowWidth"]?.let { AppSettingsStore.playerWindowWidth = (it as Number).toFloat() }
                            settings["playerWindowHeight"]?.let { AppSettingsStore.playerWindowHeight = (it as Number).toFloat() }
                            settings["playerWindowX"]?.let { AppSettingsStore.playerWindowX = (it as Number).toFloat() }
                            settings["playerWindowY"]?.let { AppSettingsStore.playerWindowY = (it as Number).toFloat() }
                            settings["playerIsFullscreen"]?.let { AppSettingsStore.playerIsFullscreen = it as Boolean }
                            settings["playerWindowAspectRatio"]?.let { AppSettingsStore.playerWindowAspectRatio = it as String }
                            
                            call.respond(HttpStatusCode.OK)
                        }
                        
                        // Generic API Proxy
                        // NOTE: This is a simplified proxy. In a real scenario, you'd map specific endpoints.
                        // For now, we assume Flutter knows what it's doing and calls specific methods if we expose them via RPC,
                        // or we can just expose specific endpoints that map to FnOfficialApi calls.
                        // Given the complexity, let's expose specific needed endpoints.
                        
                        // Example: PlayPlay (Report playback status)
                        post("/api/play/play") {
                             // This would require serializing/deserializing requests.
                             // For simplicity in this iteration, we can implement specific logic here.
                             call.respond(HttpStatusCode.NotImplemented)
                        }
                    }
                }.start(wait = true)
            } catch (e: Exception) {
                logger.e(e) { "Failed to start Bridge Server" }
                isServerRunning.set(false)
            }
        }
    }

    fun launchExternalPlayer(url: String, title: String, startPos: Long) {
        val platform = currentPlatformDesktop()
        startServer() // Ensure server is running

        // Try to send to existing player first
        if (sendToExistingPlayer(url, title, startPos)) {
            logger.i { "Successfully sent play request to existing external player." }
            return
        }

        val executableDir = ExecutableDirectoryDetector.INSTANCE.getExecutableDirectory()
        val candidates = buildExternalPlayerCandidates(platform, executableDir)
        val playerExe = candidates.firstOrNull { it.exists() }
            ?: throw IllegalStateException(
                "External player executable not found. Tried: ${candidates.joinToString { it.absolutePath }}"
            )

        logger.i { "Launching new external player instance: $url, title: $title, startPos: $startPos ms" }
        try {
            // Kill previous process if it exists (safety)
            externalPlayerProcess?.destroy()

            // Keep working directory next to the executable so Flutter can find "data/" and native libs.
            val process = ProcessBuilder(playerExe.absolutePath, url, title, startPos.toString())
                .directory(playerExe.parentFile)
                .start()
            
            externalPlayerProcess = process

            // Ensure subprocess is killed when main app exits
            Runtime.getRuntime().addShutdownHook(Thread {
                process.destroy()
            })
        } catch (e: Exception) {
            logger.e(e) { "Failed to start external player process." }
            throw e
        }
    }

    private fun sendToExistingPlayer(url: String, title: String, startPos: Long): Boolean {
        return try {
            val connection = URL("http://127.0.0.1:$PLAYER_PORT/play").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 500 // Short timeout
            connection.readTimeout = 1000

            val json = """
                {
                    "url": "$url",
                    "title": "$title",
                    "startPos": $startPos
                }
            """.trimIndent()

            connection.outputStream.use { it.write(json.toByteArray()) }
            val responseCode = connection.responseCode
            responseCode == 200
        } catch (e: Exception) {
            // If connection fails, player probably not running
            false
        }
    }

    private fun buildExternalPlayerCandidates(
        platform: Platform.Desktop,
        executableDir: File
    ): List<File> {
        val exeNames = when (platform) {
            is Platform.Windows -> listOf("flutter-player.exe", "flutter_player.exe")
            is Platform.MacOS -> listOf(
                "flutter-player.app/Contents/MacOS/flutter-player",
                "flutter_player.app/Contents/MacOS/flutter_player"
            )
            is Platform.Linux -> listOf("flutter-player", "flutter_player")
        }

        val packagedResources = File(File(executableDir, "app"), "resources")
        val packagedExes = exeNames.map { File(packagedResources, it) }

        val userDir = File(System.getProperty("user.dir") ?: ".").absoluteFile
        val projectRoot = findProjectRoot(userDir)

        val devBuildExes = projectRoot?.let { root ->
            val flutterDir = if (File(root, "flutter-player").exists()) "flutter-player" else "flutter_player"
            exeNames.flatMap { name ->
                when (platform) {
                    is Platform.Windows -> listOf(
                        File(root, "$flutterDir/build/windows/x64/runner/Release/$name"),
                        File(root, "$flutterDir/build/windows/runner/Release/$name"),
                    )
                    is Platform.MacOS -> {
                        val appName = name.split("/").first()
                        val binaryName = name.split("/").last()
                        listOf(
                            File(root, "$flutterDir/build/macos/Build/Products/Release/$appName/Contents/MacOS/$binaryName"),
                        )
                    }
                    is Platform.Linux -> listOf(
                        File(root, "$flutterDir/build/linux/x64/release/bundle/$name"),
                    )
                }
            }
        }.orEmpty()

        val bundledExes = projectRoot?.let { root ->
            exeNames.flatMap { name ->
                listOf(
                    File(root, "composeApp/build/compose/all-app-resources/$name"),
                    File(root, "composeApp/build/compose/all-app-resources/${name.replace(".exe", "")}"),
                )
            }
        }.orEmpty()

        return packagedExes + bundledExes + devBuildExes
    }

    private fun findProjectRoot(startDir: File): File? {
        var current: File? = startDir
        repeat(8) {
            val dir = current ?: return null
            if (File(dir, "flutter-player").exists() || File(dir, "flutter_player").exists()) return dir
            current = dir.parentFile
        }
        return null
    }
}
