package com.jankinwu.fntv.client.viewmodel

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jankinwu.fntv.client.data.model.request.AnalyzeRequest
import com.jankinwu.fntv.client.data.model.request.QueuedEpisode
import com.jankinwu.fntv.client.data.network.impl.FlyNarwhalApiImpl
import com.jankinwu.fntv.client.data.network.impl.FnOfficialApiImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class SmartAnalysisViewModel : BaseViewModel() {

    private val fnOfficialApi: FnOfficialApiImpl by inject(FnOfficialApiImpl::class.java)
    private val flyNarwhalApi = FlyNarwhalApiImpl()

    private val _analyzeState = MutableStateFlow<UiState<String>>(UiState.Initial)
    val analyzeState: StateFlow<UiState<String>> = _analyzeState.asStateFlow()

    private val logger = Logger.withTag("SmartAnalysisViewModel")

    fun analyzeSeason(seasonGuid: String, mediaTitle: String, seasonNumber: Int) {
        viewModelScope.launch {
            _analyzeState.value = UiState.Loading
            try {
                logger.i { "Starting analysis for season $mediaTitle (S$seasonNumber, guid: $seasonGuid)" }
                // 1. Get Episode List
                val episodeList = executeWithLoadingAndReturn {
                     fnOfficialApi.episodeList(seasonGuid)
                }
                
                logger.i { "Found ${episodeList.size} episodes" }
                val queuedEpisodes = mutableListOf<QueuedEpisode>()
                var seasonPath = ""
                
                // 2. Iterate episodes and get stream info
                for (episode in episodeList) {
                    try {
                        // Avoid hitting rate limits
                        delay(200) 
                        val streamList = fnOfficialApi.getStreamList(episode.guid, null)
                        
                        val files = streamList.files
                        if (!files.isNullOrEmpty()) {
                             val file = files.first()
                             queuedEpisodes.add(
                                 QueuedEpisode(
                                     guid = file.guid,
                                     filePath = file.path,
                                     episodeNumber = episode.episodeNumber,
                                     seasonNumber = seasonNumber
                                 )
                             )
                             
                             if (seasonPath.isEmpty()) {
                                 // Simple logic to get parent directory
                                 val separator = if (file.path.contains("/")) '/' else '\\'
                                 seasonPath = file.path.substringBeforeLast(separator, "")
                             }
                        }
                    } catch (e: Exception) {
                        logger.e(e) { "Failed to get stream for episode ${episode.guid}" }
                        // Continue to next episode
                    }
                }
                
                if (queuedEpisodes.isEmpty()) {
                    throw Exception("No valid episodes found to analyze")
                }
                
                logger.i { "Collected ${queuedEpisodes.size} episodes for analysis. Season path: $seasonPath" }

                val request = AnalyzeRequest(
                    seasonGuid = seasonGuid,
                    seasonPath = seasonPath,
                    episodes = queuedEpisodes,
                    tvTitle = mediaTitle,
                    seasonNumber = seasonNumber
                )
                
                val result = flyNarwhalApi.analyze(request)
                if (result.isSuccess()) {
                    _analyzeState.value = UiState.Success("Analysis started")
                } else {
                    _analyzeState.value = UiState.Error(result.msg)
                }
                
            } catch (e: Exception) {
                logger.e(e) { "Analysis failed" }
                _analyzeState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun clearState() {
        _analyzeState.value = UiState.Initial
    }
}
