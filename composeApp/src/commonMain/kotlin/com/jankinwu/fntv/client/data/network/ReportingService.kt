package com.jankinwu.fntv.client.data.network

import com.jankinwu.fntv.client.BuildConfig
import com.jankinwu.fntv.client.utils.Context
import com.jankinwu.fntv.client.utils.getDeviceId
import com.jankinwu.fntv.client.utils.PlatformInfo
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import korlibs.crypto.MD5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import co.touchlab.kermit.Logger
import kotlinx.serialization.encodeToString
import kotlin.time.ExperimentalTime

/**
 * Service for reporting app launch/installation statistics to a backend.
 * Uses a unique device ID and request signing to prevent malicious abuse.
 */
class ReportingService(private val context: Context) {
    private val logger = Logger.withTag("ReportingService")
    private val settings = Settings()
    private val reportKey = "last_report_version"
    
    // Values injected during build from GitHub Secrets or environment variables
    private val apiSecret = BuildConfig.REPORT_API_SECRET
    private val reportUrl = BuildConfig.REPORT_URL

    private val client = HttpClient {
        install(ContentNegotiation) {
            jackson()
        }
    }
    
    private val json = kotlinx.serialization.json.Json {
        encodeDefaults = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Reports the app launch to the backend.
     * Ensures reporting happens only once per app version to avoid redundant traffic.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun reportLaunch() {
        val currentVersion = BuildConfig.VERSION_NAME
        val lastReportedVersion = settings.getString(reportKey, "")

        if (apiSecret.isBlank() || reportUrl.isBlank()) {
            // This is expected in development environments where secrets are not configured.
            logger.i { "Reporting skipped: apiSecret or reportUrl is not configured (normal in dev environment)" }
            return
        }

        if (lastReportedVersion == currentVersion) {
            logger.i { "Already reported for version $currentVersion" }
            return
        }

        withContext(Dispatchers.Default) {
            try {
                val deviceId = getDeviceId(context)
                val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
                
                val bodyMap = mutableMapOf<String, Any>(
                    "deviceId" to deviceId,
                    "osName" to PlatformInfo.osName,
                    "osArch" to PlatformInfo.osArch,
                    "version" to currentVersion,
                    "timestamp" to timestamp
                )
                
                // Sort keys to ensure consistent JSON string
                val sortedKeys = bodyMap.keys.sorted()
                val sortedBody = sortedKeys.associateWith { bodyMap[it]!! }
                
                val signature = generateSignature(deviceId, timestamp)

                logger.i { "Reporting launch: deviceId=$deviceId, version=$currentVersion" }
                
                val response = client.post(reportUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(sortedBody + ("signature" to signature))
                }

                if (response.status.isSuccess()) {
                    settings[reportKey] = currentVersion
                    logger.i { "Successfully reported launch for version $currentVersion" }
                } else {
                    logger.e { "Failed to report launch: ${response.status}" }
                }
                
            } catch (e: Exception) {
                logger.e(e) { "Error reporting launch" }
            }
        }
    }

    /**
     * Generates a MD5 signature to verify the request integrity.
     * The signature is a hash of (compressed JSON body + secret).
     */
    private fun generateSignature(deviceId: String, timestamp: Long): String {
        val currentVersion = BuildConfig.VERSION_NAME
        val bodyMap = mutableMapOf<String, Any>(
            "deviceId" to deviceId,
            "osName" to PlatformInfo.osName,
            "osArch" to PlatformInfo.osArch,
            "version" to currentVersion,
            "timestamp" to timestamp
        )
        
        // Sort keys to ensure consistent JSON string
        val sortedKeys = bodyMap.keys.sorted()
        val sortedBody = sortedKeys.associateWith { bodyMap[it]!! }
        
        // Generate signature using compressed JSON string
        val jsonString = json.encodeToString(kotlinx.serialization.json.JsonObject(
            sortedBody.mapValues { (_, value) -> 
                when(value) {
                    is String -> kotlinx.serialization.json.JsonPrimitive(value)
                    is Long -> kotlinx.serialization.json.JsonPrimitive(value)
                    else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                }
            }
        ))
        
        return MD5.digest((jsonString + apiSecret).encodeToByteArray()).hex
    }
}
