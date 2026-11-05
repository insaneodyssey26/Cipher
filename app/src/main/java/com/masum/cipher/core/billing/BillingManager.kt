package com.masum.cipher.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.masum.cipher.core.data.local.pref.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_LIFETIME = "lifetime_plan"
        const val PRODUCT_SUBSCRIPTION = "cipher_pro_subscription"
        const val BASE_PLAN_MONTHLY = "monthly-plan"
        const val BASE_PLAN_YEARLY = "yearly-plan"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val _billingEvents = MutableSharedFlow<BillingEvent>()
    val billingEvents: SharedFlow<BillingEvent> = _billingEvents.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(pendingPurchasesParams)
        .enableAutoServiceReconnection()
        .build()

    init {
        scope.launch {
            initialize()
        }
    }

    suspend fun initialize() {
        if (ensureConnected()) {
            queryAvailableProducts()
            restorePurchases(isManual = false)
        }
    }

    private suspend fun ensureConnected(): Boolean = suspendCancellableCoroutine { continuation ->
        if (billingClient.isReady) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (continuation.isActive) {
                    continuation.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }
            }

            override fun onBillingServiceDisconnected() {
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        })
    }

    suspend fun queryAvailableProducts() = withContext(Dispatchers.IO) {
        if (!ensureConnected()) return@withContext

        val inAppProduct = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_LIFETIME)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(inAppProduct))
            .build()

        val inAppResult = billingClient.queryProductDetails(inAppParams)

        val subProduct = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_SUBSCRIPTION)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(subProduct))
            .build()

        val subResult = billingClient.queryProductDetails(subParams)

        val resultMap = mutableMapOf<String, ProductDetails>()
        inAppResult.productDetailsList?.forEach { details ->
            resultMap[details.productId] = details
        }
        subResult.productDetailsList?.forEach { details ->
            resultMap[details.productId] = details
        }

        _productDetailsMap.value = resultMap
    }

    fun getLifetimePrice(): String? {
        val details = _productDetailsMap.value[PRODUCT_LIFETIME]
        return details?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    fun getMonthlyPrice(): String? {
        val details = _productDetailsMap.value[PRODUCT_SUBSCRIPTION] ?: return null
        val offer = details.subscriptionOfferDetails?.find { it.basePlanId == BASE_PLAN_MONTHLY }
        return offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
    }

    fun getYearlyPrice(): String? {
        val details = _productDetailsMap.value[PRODUCT_SUBSCRIPTION] ?: return null
        val offer = details.subscriptionOfferDetails?.find { it.basePlanId == BASE_PLAN_YEARLY }
        return offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
    }

    fun launchPurchaseFlow(
        activity: Activity,
        productId: String,
        basePlanId: String? = null
    ): Boolean {
        scope.launch {
            if (!ensureConnected()) {
                _billingEvents.emit(BillingEvent.Error("Billing service unavailable. Please check Google Play."))
                return@launch
            }
            if (_productDetailsMap.value[productId] == null) {
                queryAvailableProducts()
            }
            withContext(Dispatchers.Main) {
                launchPurchaseFlowInternal(activity, productId, basePlanId)
            }
        }
        return true
    }

    private fun launchPurchaseFlowInternal(
        activity: Activity,
        productId: String,
        basePlanId: String?
    ): Boolean {
        val details = _productDetailsMap.value[productId]
        if (details == null) {
            scope.launch {
                _billingEvents.emit(BillingEvent.Error("Product details not available yet."))
            }
            return false
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (details.productType == BillingClient.ProductType.SUBS) {
            val offer = if (basePlanId != null) {
                details.subscriptionOfferDetails?.find { it.basePlanId == basePlanId }
            } else {
                details.subscriptionOfferDetails?.firstOrNull()
            }

            val offerToken = offer?.offerToken
            if (offerToken == null) {
                scope.launch {
                    _billingEvents.emit(BillingEvent.Error("Subscription plan unavailable."))
                }
                return false
            }
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    scope.launch {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                scope.launch {
                    _billingEvents.emit(BillingEvent.Cancelled)
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                scope.launch {
                    restorePurchases(isManual = false)
                    _billingEvents.emit(BillingEvent.AlreadyOwned)
                }
            }
            else -> {
                scope.launch {
                    _billingEvents.emit(BillingEvent.Error(billingResult.debugMessage.ifBlank { "Purchase failed" }))
                }
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val ackResult = billingClient.acknowledgePurchase(acknowledgeParams)
                if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    return
                }
            }

            val hasLifetime = purchase.products.contains(PRODUCT_LIFETIME)
            val hasSubscription = purchase.products.contains(PRODUCT_SUBSCRIPTION)

            val tier = if (hasLifetime) "LIFETIME" else if (hasSubscription) "SUBSCRIBER" else "PRO"

            userPreferences.setProStatus(
                isPro = true,
                tier = tier,
                token = purchase.orderId ?: purchase.purchaseToken,
                orderId = purchase.orderId,
                expiresAt = 0L
            )

            _billingEvents.emit(BillingEvent.Success(tier))
        }
    }

    fun restorePurchases(isManual: Boolean = true) {
        scope.launch {
            _isLoading.value = true
            try {
                if (!ensureConnected()) {
                    if (isManual) {
                        _billingEvents.emit(BillingEvent.Error("Unable to connect to Google Play."))
                    }
                    return@launch
                }

                val inAppParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                val inAppResult = billingClient.queryPurchasesAsync(inAppParams)

                val subParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
                val subResult = billingClient.queryPurchasesAsync(subParams)

                val allPurchases = mutableListOf<Purchase>()
                if (inAppResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    allPurchases.addAll(inAppResult.purchasesList)
                }
                if (subResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    allPurchases.addAll(subResult.purchasesList)
                }

                val activePurchase = allPurchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }

                if (activePurchase != null) {
                    handlePurchase(activePurchase)
                    if (isManual) {
                        _billingEvents.emit(BillingEvent.Restored(true))
                    }
                } else {
                    if (isManual) {
                        _billingEvents.emit(BillingEvent.Restored(false))
                    }
                }
            } catch (e: Exception) {
                if (isManual) {
                    _billingEvents.emit(BillingEvent.Error(e.message ?: "Failed to restore purchases."))
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

sealed class BillingEvent {
    data class Success(val tier: String) : BillingEvent()
    data class Restored(val hasPro: Boolean) : BillingEvent()
    data object Cancelled : BillingEvent()
    data object AlreadyOwned : BillingEvent()
    data class Error(val message: String) : BillingEvent()
}
