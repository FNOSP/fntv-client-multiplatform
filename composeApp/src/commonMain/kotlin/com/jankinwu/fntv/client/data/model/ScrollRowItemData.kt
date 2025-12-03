package com.jankinwu.fntv.client.data.model

/**
 * 用于保存横向滚动行中item的简单数据类
 */
data class ScrollRowItemData(
    val title: String,
    val subtitle: String? = null,
    val score: String? = null,
    val resolutions: List<String>? = null,
    val posterImg: String? = "",
    val isFavourite: Boolean = false,
    val isAlreadyWatched: Boolean = false,
    // 时长（秒）
    val duration: Int = 0,
    // 观看时间（秒）
    val ts: Long = 0,
    val posters: List<String>? = null,
    val guid: String,
    val isVisible: Boolean = true,
    val posterHeight: Int = 0,
    val posterWidth: Int = 0,
    val status: String? = "",
    val type: String? = "",
    val parentGuid: String? = ""
)
