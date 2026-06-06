package com.masum.cipher.core.domain.model

data class ParsedTransaction(
    val amount: Double,
    val merchant: String,
    val currency: String,
    val isIncome: Boolean
)