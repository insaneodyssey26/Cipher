package com.masum.cipher.core.ml

interface MessageIntentClassifier {
    fun classify(message: String): IntentClassificationResult
}
