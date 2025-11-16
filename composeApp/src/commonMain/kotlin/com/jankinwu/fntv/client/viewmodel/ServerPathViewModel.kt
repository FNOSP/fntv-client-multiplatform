package com.jankinwu.fntv.client.viewmodel

import androidx.lifecycle.viewModelScope
import com.jankinwu.fntv.client.data.model.response.ServerPathResponse
import com.jankinwu.fntv.client.data.network.impl.FnOfficialApiImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class ServerPathViewModel : BaseViewModel() {

    private val fnOfficialApi: FnOfficialApiImpl by inject(FnOfficialApiImpl::class.java)

    private val _uiState = MutableStateFlow<UiState<List<ServerPathResponse>>>(UiState.Initial)
    val uiState: StateFlow<UiState<List<ServerPathResponse>>> = _uiState.asStateFlow()

    fun loadFilesByServerPath(path: String) {
        viewModelScope.launch {
            executeWithLoading(_uiState) {
                fnOfficialApi.getFilesByServerPath(path)
            }
        }
    }

    fun refreshFilesByServerPath(path: String) {
        loadFilesByServerPath(path)
    }

    fun clearError() {
        _uiState.value = UiState.Initial
    }
}