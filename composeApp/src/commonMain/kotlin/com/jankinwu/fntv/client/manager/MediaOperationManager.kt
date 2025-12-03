package com.jankinwu.fntv.client.manager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jankinwu.fntv.client.ui.component.common.ToastManager
import com.jankinwu.fntv.client.viewmodel.FavoriteViewModel.FavoriteActionResult
import com.jankinwu.fntv.client.viewmodel.UiState
import com.jankinwu.fntv.client.viewmodel.WatchedViewModel.WatchedActionResult

@Composable
fun HandleFavoriteResult(
    favoriteUiState: UiState<FavoriteActionResult>,
    toastManager: ToastManager,
    pendingCallbacks: Map<String, (Boolean) -> Unit>,
    onPendingCallbackHandled: (String) -> Unit,
    clearError: () -> Unit
) {
    LaunchedEffect(favoriteUiState) {
        when (val state = favoriteUiState) {
            is UiState.Success -> {
                toastManager.showToast(state.data.message, state.data.success)
                pendingCallbacks[state.data.guid]?.invoke(state.data.success)
                onPendingCallbackHandled(state.data.guid)
            }

            is UiState.Error -> {
                toastManager.showToast("操作失败，${state.message}", false)
                state.operationId?.let {
                    pendingCallbacks[state.operationId]?.invoke(false)
                    onPendingCallbackHandled(state.operationId)
                }
            }

            else -> {}
        }

        if (favoriteUiState is UiState.Success || favoriteUiState is UiState.Error) {
            kotlinx.coroutines.delay(2000)
            clearError()
        }
    }
}

@Composable
fun HandleWatchedResult(
    watchedUiState: UiState<WatchedActionResult>,
    toastManager: ToastManager,
    pendingCallbacks: Map<String, (Boolean) -> Unit>,
    onPendingCallbackHandled: (String) -> Unit,
    clearError: () -> Unit
) {
    LaunchedEffect(watchedUiState) {
        when (val state = watchedUiState) {
            is UiState.Success -> {
                toastManager.showToast(state.data.message, state.data.success)
                pendingCallbacks[state.data.guid]?.invoke(state.data.success)
                onPendingCallbackHandled(state.data.guid)
            }

            is UiState.Error -> {
                toastManager.showToast("操作失败，${state.message}", false)
                state.operationId?.let {
                    pendingCallbacks[state.operationId]?.invoke(false)
                    onPendingCallbackHandled(state.operationId)
                }
            }

            else -> {}
        }

        if (watchedUiState is UiState.Success || watchedUiState is UiState.Error) {
            kotlinx.coroutines.delay(2000)
            clearError()
        }
    }
}