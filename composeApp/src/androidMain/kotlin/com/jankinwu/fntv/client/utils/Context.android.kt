package com.jankinwu.fntv.client.utils

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.io.files.Path

actual abstract class Context {
    abstract val contentResolver: android.content.ContentResolver
    abstract val cacheDir: java.io.File
    abstract val filesDir: java.io.File
    abstract fun getExternalFilesDir(type: String?): java.io.File?
}

class AndroidContext(val androidContext: android.content.Context) : Context() {
    override val contentResolver: android.content.ContentResolver
        get() = androidContext.contentResolver
    override val cacheDir: java.io.File
        get() = androidContext.cacheDir
    override val filesDir: java.io.File
        get() = androidContext.filesDir
    override fun getExternalFilesDir(type: String?): java.io.File? = androidContext.getExternalFilesDir(type)
}

actual val LocalContext: ProvidableCompositionLocal<Context> = staticCompositionLocalOf {
    error("No Context provided")
}

internal actual val Context.filesImpl: ContextFiles
    get() = object : ContextFiles {
        override val cacheDir: SystemPath = Path(this@filesImpl.cacheDir.absolutePath).inSystem
        override val dataDir: SystemPath = Path(this@filesImpl.filesDir.absolutePath).inSystem
        override val defaultBaseMediaCacheDir: SystemPath = 
            Path((this@filesImpl.getExternalFilesDir(null) ?: this@filesImpl.filesDir).absolutePath).inSystem
    }