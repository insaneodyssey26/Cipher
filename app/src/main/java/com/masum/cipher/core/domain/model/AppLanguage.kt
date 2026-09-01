package com.masum.cipher.core.domain.model

data class AppLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val countryCode: String
) {
    companion object {
        val SYSTEM = AppLanguage("system", "System Default", "Default", "SYS")
        val ENGLISH = AppLanguage("en", "English", "English", "EN")
        val HINDI = AppLanguage("hi", "Hindi", "हिन्दी", "HI")
        val BENGALI = AppLanguage("bn", "Bengali", "বাংলা", "BN")
        val SPANISH = AppLanguage("es", "Spanish", "Español", "ES")
        val FRENCH = AppLanguage("fr", "French", "Français", "FR")
        val GERMAN = AppLanguage("de", "German", "Deutsch", "DE")
        val JAPANESE = AppLanguage("ja", "Japanese", "日本語", "JA")

        val SUPPORTED_LANGUAGES = listOf(
            SYSTEM,
            ENGLISH,
            HINDI,
            BENGALI,
            SPANISH,
            FRENCH,
            GERMAN,
            JAPANESE
        )

        fun fromCode(code: String): AppLanguage {
            return SUPPORTED_LANGUAGES.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: SYSTEM
        }
    }
}
