package com.masum.cipher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import com.masum.cipher.core.util.CrashReporter

@HiltAndroidApp
class CipherSpendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.init(this)
        System.loadLibrary("sqlcipher")
    }
}