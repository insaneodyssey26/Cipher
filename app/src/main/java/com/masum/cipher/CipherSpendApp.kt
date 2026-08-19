package com.masum.cipher

import android.app.Application
import com.masum.cipher.core.util.CrashReporter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CipherSpendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.init(this)
        System.loadLibrary("sqlcipher")
    }
}