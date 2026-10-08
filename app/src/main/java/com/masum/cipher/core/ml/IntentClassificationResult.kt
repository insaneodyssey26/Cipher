package com.masum.cipher.core.ml

data class IntentClassificationResult(
    val intent: MessageIntent,
    val confidence: Float,
    val probabilities: Map<MessageIntent, Float>
) {
    val isExpense: Boolean get() = intent == MessageIntent.TRANSACTION_EXPENSE
    val isIncome: Boolean get() = intent == MessageIntent.TRANSACTION_INCOME
    val isPromotional: Boolean get() = intent == MessageIntent.PROMOTIONAL_MARKETING
    val isInformational: Boolean get() = intent == MessageIntent.INFORMATIONAL_OTP
}
