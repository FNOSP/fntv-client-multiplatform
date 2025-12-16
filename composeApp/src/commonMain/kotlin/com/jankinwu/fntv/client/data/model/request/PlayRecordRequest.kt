package com.jankinwu.fntv.client.data.model.request

import androidx.compose.runtime.Immutable
import com.fasterxml.jackson.annotation.JsonProperty

@Immutable
data class PlayRecordRequest(
    @param:JsonProperty("item_guid")
    val itemGuid: String,

    @param:JsonProperty("media_guid")
    val mediaGuid: String,

    @param:JsonProperty("video_guid")
    val videoGuid: String,

    @param:JsonProperty("audio_guid")
    val audioGuid: String,

    @param:JsonProperty("subtitle_guid")
    val subtitleGuid: String?,

    @param:JsonProperty("resolution")
    val resolution: String,

    @param:JsonProperty("bitrate")
    val bitrate: Int,

    @param:JsonProperty("ts")
    val ts: Int,

    @param:JsonProperty("duration")
    val duration: Int,

    @param:JsonProperty("play_link")
    val playLink: String? = null
)