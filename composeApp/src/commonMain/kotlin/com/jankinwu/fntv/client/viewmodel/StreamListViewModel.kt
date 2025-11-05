package com.jankinwu.fntv.client.viewmodel

import androidx.lifecycle.viewModelScope
import com.jankinwu.fntv.client.data.model.response.StreamListResponse
import com.jankinwu.fntv.client.data.network.impl.FnOfficialApiImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class StreamListViewModel : BaseViewModel() {

    private val fnOfficialApi: FnOfficialApiImpl by inject(FnOfficialApiImpl::class.java)

    private val _uiState = MutableStateFlow<UiState<StreamListResponse>>(UiState.Initial)
    val uiState: StateFlow<UiState<StreamListResponse>> = _uiState.asStateFlow()

    fun loadData(guid: String, beforePlay: Int? = null) {
        viewModelScope.launch {
            executeWithLoading(_uiState) {
                fnOfficialApi.getStreamList(guid, beforePlay)
            }
        }
    }

    fun refresh(guid: String, beforePlay: Int? = null) {
        loadData(guid, beforePlay)
    }

    fun clearError() {
        _uiState.value = UiState.Initial
    }
}