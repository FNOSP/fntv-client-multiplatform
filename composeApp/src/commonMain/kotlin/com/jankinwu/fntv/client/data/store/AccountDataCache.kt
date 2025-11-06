package com.jankinwu.fntv.client.data.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

object AccountDataCache {

    var authorization: String = ""

    var cookieMap: MutableMap<String, String> = mutableMapOf()
    
    private var _cookieState = mutableStateOf("")
    val cookieState: String by _cookieState

    var userName: String = ""

    var password: String = ""

    var isHttps: Boolean = false

    var host: String = ""

    var port: Int = 0

    var isLoggedIn: Boolean = false

    var rememberMe: Boolean = false

    fun getFnOfficialBaseUrl(): String {
        var endpoint = host
        if (port != 0) {
            endpoint = "$endpoint:$port"
        }
        return if (isHttps) {
            "https://$endpoint"
        } else {
            "http://$endpoint"
        }
    }

    private fun getCookie(): String {
        return cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
    }
    
    fun updateCookie(pair: Pair<String, String>) {
        println("update cookie map：$pair")
        cookieMap[pair.first] = pair.second
        _cookieState.value = getCookie()
    }

    fun clearCookie() {
        cookieMap.clear()
        _cookieState.value = ""
    }
}