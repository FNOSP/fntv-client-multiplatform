package com.jankinwu.fntv.client.viewmodel

import androidx.lifecycle.viewModelScope
import com.jankinwu.fntv.client.data.store.AppSettingsStore
import com.jankinwu.fntv.client.manager.UpdateInfo
import com.jankinwu.fntv.client.manager.UpdateManager
import org.koin.java.KoinJavaComponent.inject

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UpdateViewModel : BaseViewModel() {
    private val updateManager: UpdateManager by inject(UpdateManager::class.java)
    val status = updateManager.status
    val latestVersion = updateManager.latestVersion

    private var lastCheckTime = 0L
    private var scheduledCheckJob: Job? = null
    private val checkInterval = 5 * 60 * 1000L // 5 minutes

    fun checkUpdate() {
        lastCheckTime = System.currentTimeMillis()
        val proxyUrl = AppSettingsStore.githubResourceProxyUrl
        val includePrerelease = AppSettingsStore.includePrerelease
        updateManager.checkUpdate(proxyUrl, includePrerelease)
    }

    fun onIncludePrereleaseChanged() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime >= checkInterval) {
            // No restriction, check immediately
            checkUpdate()
            scheduledCheckJob?.cancel()
        } else {
            // Restricted, schedule a check if not already scheduled
            if (scheduledCheckJob?.isActive != true) {
                val delayTime = checkInterval - (currentTime - lastCheckTime)
                scheduledCheckJob = viewModelScope.launch {
                    delay(delayTime)
                    // Double check if we still need to run (in case a manual check happened)
                    if (System.currentTimeMillis() - lastCheckTime >= checkInterval) {
                        checkUpdate()
                    }
                }
            }
        }
    }

    fun downloadUpdate(info: UpdateInfo) {
        val proxyUrl = AppSettingsStore.githubResourceProxyUrl
        updateManager.downloadUpdate(proxyUrl, info)
    }
    
    fun installUpdate(info: UpdateInfo) {
        updateManager.installUpdate(info)
    }

    fun cancelDownload() {
        updateManager.cancelDownload()
    }
    
    fun clearStatus() {
        updateManager.clearStatus()
    }
}
