package com.masum.cipher.core.util

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

object CrashReporter {
    private const val CRASH_FILE_NAME = "cipher_crash_log.txt"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                val crashFile = File(context.filesDir, CRASH_FILE_NAME)
                
                val sw = StringWriter()
                exception.printStackTrace(PrintWriter(sw))
                val stackTrace = sw.toString()
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val timestamp = dateFormat.format(Date())
                
                val logContent = "--- CIPHER CRASH LOG ---\n" +
                               "Time: $timestamp\n" +
                               "Thread: ${thread.name}\n\n" +
                               "$stackTrace\n"
                               
                crashFile.writeText(logContent)
            } catch (e: Exception) {
                // Ignore, app is already crashing
            }
            
            // Let the default handler finish the crash (shows standard Android crash dialog/force close)
            defaultHandler?.uncaughtException(thread, exception)
            if (defaultHandler == null) {
                exitProcess(1)
            }
        }
    }

    fun getCrashLog(context: Context): String? {
        val crashFile = File(context.filesDir, CRASH_FILE_NAME)
        return if (crashFile.exists()) {
            crashFile.readText()
        } else {
            null
        }
    }

    fun clearCrashLog(context: Context) {
        val crashFile = File(context.filesDir, CRASH_FILE_NAME)
        if (crashFile.exists()) {
            crashFile.delete()
        }
    }
}
