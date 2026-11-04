package com.masum.cipher.core.domain.model

data class ParsedTransaction(
    val amount: Double,
    val merchant: String,
    val currency: String,
    val isIncome: Boolean,
    val accountLast4: String? = null,
    val confidence: Float = 1.0f,
    val isAmbiguous: Boolean = false
)