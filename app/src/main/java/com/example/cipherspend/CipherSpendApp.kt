package com.example.cipherspend

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CipherSpendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
    }
}