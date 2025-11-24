package com.jankinwu.fntv.client.data.model.response

import androidx.compose.runtime.Immutable
import com.fasterxml.jackson.annotation.JsonProperty

@Immutable
data class MediaItemResponse(
    @param:JsonProperty("media_guid")
    val mediaGuid: String,

    @param:JsonProperty("item_guid")
    val itemGuid: String,

    @param:JsonProperty("filename")
    val filename: String,

    @param:JsonProperty("file_path")
    val filePath: String,

    @param:JsonProperty("duration")
    val duration: Int,

    @param:JsonProperty("create_time")
    val createTime: Long,

    @param:JsonProperty("media_stream")
    val mediaStream: MediaStreamResponse?,
)

@Immutable
data class MediaStreamResponse(
    @param:JsonProperty("resolutions")
    val resolutions: List<String>?,

    @param:JsonProperty("audio_type")
    val audioType: List<String>?,

    @param:JsonProperty("color_range_type")
    val colorRangeType: List<String>?,
)