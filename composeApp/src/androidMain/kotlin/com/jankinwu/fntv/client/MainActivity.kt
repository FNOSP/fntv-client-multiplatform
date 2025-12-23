package com.jankinwu.fntv.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.jankinwu.fntv.client.utils.AndroidContext
import com.jankinwu.fntv.client.utils.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val androidContext = androidx.compose.ui.platform.LocalContext.current
            val contextMP = remember(androidContext) { AndroidContext(androidContext) }
            CompositionLocalProvider(LocalContext provides contextMP) {
                // App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
//    App()
}