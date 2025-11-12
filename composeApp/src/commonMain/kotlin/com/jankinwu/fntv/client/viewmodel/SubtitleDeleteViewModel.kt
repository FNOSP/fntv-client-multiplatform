package com.jankinwu.fntv.client.viewmodel

import androidx.lifecycle.viewModelScope
import com.jankinwu.fntv.client.data.network.impl.FnOfficialApiImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class SubtitleDeleteViewModel : BaseViewModel() {

    private val fnOfficialApi: FnOfficialApiImpl by inject(FnOfficialApiImpl::class.java)

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Initial)
    val uiState: StateFlow<UiState<Boolean>> = _uiState.asStateFlow()

    fun deleteSubtitle(subtitleGuid: String) {
        viewModelScope.launch {
            executeWithLoading(_uiState, operationId = subtitleGuid) {
                fnOfficialApi.deleteSubtitle(subtitleGuid)
            }
        }
    }

    fun clearError() {
        _uiState.value = UiState.Initial
    }
}