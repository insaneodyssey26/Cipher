package com.masum.cipher.core.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    fun setLocale(languageCode: String) {
        val appLocales = if (languageCode.isBlank() || languageCode.equals("system", ignoreCase = true)) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    fun wrapContext(context: Context, languageCode: String): Context {
        if (languageCode.isBlank() || languageCode.equals("system", ignoreCase = true)) {
            return context
        }
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocales(android.os.LocaleList(locale))
        return context.createConfigurationContext(config)
    }

    fun getOverrideConfiguration(context: Context, languageCode: String): Configuration? {
        if (languageCode.isBlank() || languageCode.equals("system", ignoreCase = true)) {
            return null
        }
        val locale = Locale.forLanguageTag(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocales(android.os.LocaleList(locale))
        return config
    }
}
