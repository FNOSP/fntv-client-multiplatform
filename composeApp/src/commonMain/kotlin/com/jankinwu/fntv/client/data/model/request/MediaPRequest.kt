package com.jankinwu.fntv.client.data.model.request

import androidx.compose.runtime.Immutable
import com.fasterxml.jackson.annotation.JsonProperty

@Immutable
data class MediaPRequest(
    @param:JsonProperty("req")
    var req: String?,

    @param:JsonProperty("reqid")
    var reqId: String?,

    @param:JsonProperty("playLink")
    val playLink: String,

    @param:JsonProperty("quality")
    val quality: Quality?,

    @param:JsonProperty("startTimestamp")
    val startTimestamp: Int?,

    @param:JsonProperty("clearCache")
    val clearCache: Boolean?,

    @param:JsonProperty("audioEncoder")
    val audioEncoder: String? = null,

    @param:JsonProperty("channels")
    val channels: Int? = null,

    @param:JsonProperty("audioIndex")
    val audioIndex: Int? = null
) {
    @Immutable
    data class Quality(
        @param:JsonProperty("resolution")
        val resolution: String,

        @param:JsonProperty("bitrate")
        val bitrate: Int
    )
}