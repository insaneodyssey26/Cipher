package com.masum.cipher.core.billing

import android.util.Base64
import com.android.billingclient.api.Purchase
import com.masum.cipher.BuildConfig
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseVerifier @Inject constructor() {

    fun verify(purchase: Purchase): Boolean {
        if (BuildConfig.DEBUG && BuildConfig.PLAY_LICENSE_PUBLIC_KEY.isBlank()) {
            return true
        }

        val base64PublicKey = BuildConfig.PLAY_LICENSE_PUBLIC_KEY
        if (base64PublicKey.isBlank()) {
            return false
        }

        val signedData = purchase.originalJson
        val signature = purchase.signature

        if (signedData.isBlank() || signature.isBlank()) {
            return false
        }

        val publicKey = generatePublicKey(base64PublicKey) ?: return false
        return verifySignature(publicKey, signedData, signature)
    }

    private fun generatePublicKey(encodedPublicKey: String): PublicKey? {
        return try {
            val decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (_: Exception) {
            null
        }
    }

    private fun verifySignature(publicKey: PublicKey, signedData: String, signature: String): Boolean {
        return try {
            val signatureBytes = Base64.decode(signature, Base64.DEFAULT)
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray(Charsets.UTF_8))
            sig.verify(signatureBytes)
        } catch (_: Exception) {
            false
        }
    }
}
