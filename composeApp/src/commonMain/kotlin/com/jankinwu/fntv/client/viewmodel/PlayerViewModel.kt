package com.jankinwu.fntv.client.viewmodel

import androidx.lifecycle.ViewModel
import com.jankinwu.fntv.client.data.model.PlayingInfoCache
import com.jankinwu.fntv.client.data.model.SubtitleSettings
import com.jankinwu.fntv.client.data.model.response.StreamResponse
import com.jankinwu.fntv.client.data.model.response.SubtitleStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerViewModel : ViewModel() {
    private val _playingInfoCache = MutableStateFlow<PlayingInfoCache?>(null)
    val playingInfoCache: StateFlow<PlayingInfoCache?> = _playingInfoCache.asStateFlow()

    private val _subtitleSettings = MutableStateFlow(SubtitleSettings())
    val subtitleSettings: StateFlow<SubtitleSettings> = _subtitleSettings.asStateFlow()

    fun updateSubtitleSettings(settings: SubtitleSettings) {
        _subtitleSettings.value = settings
    }

    fun updatePlayingInfo(info: PlayingInfoCache?) {
        // 当切换视频（guid不同）或清除播放信息时，重置字幕设置
        if (info?.itemGuid != _playingInfoCache.value?.itemGuid) {
            _subtitleSettings.value = SubtitleSettings()
        }
        _playingInfoCache.value = info
    }

    fun updateSubtitleList(subtitleStreams: List<SubtitleStream>, streamInfo: StreamResponse) {
        _playingInfoCache.update { current ->
            current?.copy(
                currentSubtitleStreamList = subtitleStreams,
                streamInfo = streamInfo
            )
        }
    }
}
