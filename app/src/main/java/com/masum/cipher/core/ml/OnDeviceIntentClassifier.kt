package com.masum.cipher.core.ml

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

@Serializable
private data class IntentModelPayload(
    val version: Int = 1,
    val vocab: Map<String, Int>,
    val idf: List<Float>,
    val weights: List<List<Float>>,
    val biases: List<Float>
)

@Singleton
class OnDeviceIntentClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) : MessageIntentClassifier {

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val vocab = HashMap<String, Int>(3000)
    private var idf: FloatArray = FloatArray(0)
    private var weights: Array<FloatArray> = emptyArray()
    private var biases: FloatArray = FloatArray(4)

    @Volatile
    private var isInitialized = false

    private fun ensureInitialized() {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                context.assets.open("ml/intent_classifier.json").use { inputStream ->
                    InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                        val content = reader.readText()
                        val payload = jsonParser.decodeFromString<IntentModelPayload>(content)
                        vocab.putAll(payload.vocab)
                        idf = payload.idf.toFloatArray()
                        weights = Array(payload.weights.size) { i -> payload.weights[i].toFloatArray() }
                        biases = payload.biases.toFloatArray()
                        isInitialized = true
                    }
                }
            } catch (e: Exception) {
                isInitialized = false
            }
        }
    }

    override fun classify(message: String): IntentClassificationResult {
        ensureInitialized()
        if (!isInitialized || vocab.isEmpty() || weights.isEmpty()) {
            return NlpFeatureExtractor.fallbackClassification(message)
        }

        val features = NlpFeatureExtractor.extractFeatures(message)
        if (features.isEmpty()) {
            return NlpFeatureExtractor.fallbackClassification(message)
        }

        val counts = HashMap<Int, Float>()
        for (f in features) {
            val idx = vocab[f] ?: continue
            counts[idx] = (counts[idx] ?: 0f) + 1f
        }

        if (counts.isEmpty()) {
            return NlpFeatureExtractor.fallbackClassification(message)
        }

        var sumSq = 0f
        for ((idx, count) in counts) {
            val tfIdf = count * idf[idx]
            counts[idx] = tfIdf
            sumSq += tfIdf * tfIdf
        }

        val norm = if (sumSq > 0f) sqrt(sumSq) else 1f

        val logits = FloatArray(4) { biases[it] }
        for ((idx, tfIdf) in counts) {
            val normalizedVal = tfIdf / norm
            val weightRow = weights[idx]
            for (c in 0 until 4) {
                logits[c] += normalizedVal * weightRow[c]
            }
        }

        val maxLogit = max(max(logits[0], logits[1]), max(logits[2], logits[3]))
        val expLogits = FloatArray(4) { exp(logits[it] - maxLogit) }
        val sumExp = expLogits[0] + expLogits[1] + expLogits[2] + expLogits[3]

        val probs = FloatArray(4) { if (sumExp > 0f) expLogits[it] / sumExp else 0.25f }

        var bestClass = 0
        var maxProb = probs[0]
        for (c in 1 until 4) {
            if (probs[c] > maxProb) {
                maxProb = probs[c]
                bestClass = c
            }
        }

        val intent = when (bestClass) {
            0 -> MessageIntent.TRANSACTION_EXPENSE
            1 -> MessageIntent.TRANSACTION_INCOME
            2 -> MessageIntent.PROMOTIONAL_MARKETING
            else -> MessageIntent.INFORMATIONAL_OTP
        }

        val probMap = mapOf(
            MessageIntent.TRANSACTION_EXPENSE to probs[0],
            MessageIntent.TRANSACTION_INCOME to probs[1],
            MessageIntent.PROMOTIONAL_MARKETING to probs[2],
            MessageIntent.INFORMATIONAL_OTP to probs[3]
        )

        return IntentClassificationResult(
            intent = intent,
            confidence = maxProb,
            probabilities = probMap
        )
    }
}
