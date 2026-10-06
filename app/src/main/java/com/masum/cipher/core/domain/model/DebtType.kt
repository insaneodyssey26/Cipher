package com.masum.cipher.core.domain.model

enum class DebtType(val key: String) {
    LENT("LENT"),
    BORROWED("BORROWED");

    companion object {
        fun fromKey(key: String): DebtType {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: LENT
        }
    }
}
