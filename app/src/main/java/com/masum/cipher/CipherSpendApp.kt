package com.masum.cipher

import android.app.Application
import androidx.compose.ui.AndroidComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import com.masum.cipher.core.util.CrashReporter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CipherSpendApp : Application() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate() {
        AndroidComposeUiFlags.isOutOfFrameSchedulerForTextInputEventsEnabled = false
        super.onCreate()
        CrashReporter.init(this)
        System.loadLibrary("sqlcipher")
    }
}