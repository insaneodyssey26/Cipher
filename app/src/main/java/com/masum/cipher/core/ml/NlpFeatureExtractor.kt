package com.masum.cipher.core.ml

import java.util.regex.Pattern

object NlpFeatureExtractor {

    private val numberPattern = Pattern.compile("(?i)(\\d+[\\d,]*\\.\\d+|\\d+[\\d,]*)")
    private val currencyPattern = Pattern.compile("(?i)(₹|rs\\.?|inr|\\$|€|£|aed|sgd|cad|aud)")
    private val accountPattern = Pattern.compile("(?i)(xx+|\\*+)\\d*")
    private val tokenPattern = Pattern.compile("[a-z0-9]+")

    fun extractFeatures(text: String): List<String> {
        var normalized = text.lowercase()
        normalized = numberPattern.matcher(normalized).replaceAll(" numtoken ")
        normalized = currencyPattern.matcher(normalized).replaceAll(" currtoken ")
        normalized = accountPattern.matcher(normalized).replaceAll(" acctoken ")

        val tokens = ArrayList<String>()
        val matcher = tokenPattern.matcher(normalized)
        while (matcher.find()) {
            tokens.add(matcher.group())
        }

        if (tokens.isEmpty()) return emptyList()

        val features = ArrayList<String>(tokens.size * 3)
        features.addAll(tokens)

        for (i in 0 until tokens.size - 1) {
            features.add("${tokens[i]}_${tokens[i + 1]}")
        }

        for (i in 0 until tokens.size - 2) {
            features.add("${tokens[i]}_${tokens[i + 1]}_${tokens[i + 2]}")
        }

        return features
    }

    fun fallbackClassification(message: String): IntentClassificationResult {
        val lower = message.lowercase()
        val isPromo = lower.contains("loan") || lower.contains("emi") || lower.contains("apply") || lower.contains("offer") || lower.contains("flat")
        val isOtp = lower.contains("otp") || lower.contains("verification code") || lower.contains("passcode")
        val isIncome = lower.contains("credited") || lower.contains("received") || lower.contains("deposited") || lower.contains("paid you")

        val intent = when {
            isOtp -> MessageIntent.INFORMATIONAL_OTP
            isPromo -> MessageIntent.PROMOTIONAL_MARKETING
            isIncome -> MessageIntent.TRANSACTION_INCOME
            else -> MessageIntent.TRANSACTION_EXPENSE
        }

        return IntentClassificationResult(
            intent = intent,
            confidence = 0.5f,
            probabilities = mapOf(intent to 1.0f)
        )
    }
}
