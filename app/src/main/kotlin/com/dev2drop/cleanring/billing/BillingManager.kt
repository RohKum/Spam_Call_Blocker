package com.dev2drop.cleanring.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
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
import com.dev2drop.cleanring.SpamBlockerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SubscriptionInfo(
    val productId: String,
    val formattedPrice: String,
    val billingPeriod: String,
    val productDetails: ProductDetails? = null
)

class BillingManager(
    private val context: Context,
    private val externalScope: CoroutineScope
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "CleanRingBilling"
        const val PRODUCT_MONTHLY = "clean_ring_monthly"
        const val PRODUCT_YEARLY = "clean_ring_yearly"
        const val TRIAL_DURATION_DAYS = 14
        const val TRIAL_DURATION_MS = TRIAL_DURATION_DAYS * 24L * 3600 * 1000
    }

    private val sharedPrefs = context.getSharedPreferences("clean_ring_billing_prefs", Context.MODE_PRIVATE)

    private val _isSubscribed = MutableStateFlow(sharedPrefs.getBoolean("is_subscribed", false))
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _availableSubscriptions = MutableStateFlow<List<SubscriptionInfo>>(emptyList())
    val availableSubscriptions: StateFlow<List<SubscriptionInfo>> = _availableSubscriptions.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        ensureInstallTimestamp()
        startConnection()
    }

    private fun ensureInstallTimestamp() {
        if (!sharedPrefs.contains("first_install_time")) {
            sharedPrefs.edit().putLong("first_install_time", System.currentTimeMillis()).apply()
        }
    }

    fun getFirstInstallTime(): Long {
        return sharedPrefs.getLong("first_install_time", System.currentTimeMillis())
    }

    fun isTrialActive(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        val installTime = getFirstInstallTime()
        return currentTimeMs < (installTime + TRIAL_DURATION_MS)
    }

    fun getTrialDaysRemaining(currentTimeMs: Long = System.currentTimeMillis()): Int {
        val installTime = getFirstInstallTime()
        val elapsed = currentTimeMs - installTime
        val remainingMs = TRIAL_DURATION_MS - elapsed
        if (remainingMs <= 0) return 0
        return ((remainingMs / (24L * 3600 * 1000)) + 1).toInt().coerceAtMost(TRIAL_DURATION_DAYS)
    }

    fun hasProtectionAccess(currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        return _isSubscribed.value || isTrialActive(currentTimeMs)
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Client Setup Successful")
                    queryActivePurchases()
                    queryProductDetails()
                } else {
                    Log.e(TAG, "Billing Client Setup Error: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing Service Disconnected. Retrying...")
            }
        })
    }

    fun queryActivePurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var hasActiveSubscription = false
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        hasActiveSubscription = true
                        handlePurchase(purchase)
                    }
                }
                updateSubscribedState(hasActiveSubscription)
            }
        }
    }

    fun queryProductDetails() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val subList = mutableListOf<SubscriptionInfo>()
                val productDetailsList = productDetailsResult.productDetailsList ?: emptyList()
                for (pd in productDetailsList) {
                    val offerDetails = pd.subscriptionOfferDetails?.firstOrNull()
                    val pricingPhases = offerDetails?.pricingPhases?.pricingPhaseList ?: emptyList()
                    val recurringPhase = pricingPhases.lastOrNull { it.priceAmountMicros > 0 } ?: pricingPhases.lastOrNull()
                    val price = recurringPhase?.formattedPrice ?: ""
                    subList.add(
                        SubscriptionInfo(
                            productId = pd.productId,
                            formattedPrice = price,
                            billingPeriod = if (pd.productId == PRODUCT_MONTHLY) "Month" else "Year",
                            productDetails = pd
                        )
                    )
                }
                _availableSubscriptions.value = subList
            }
        }
    }

    fun launchSubscriptionFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken.isNullOrBlank()) {
            Log.w(TAG, "Offer token not yet active on Google Play for ${productDetails.productId}. Refreshing...")
            queryProductDetails()
            Toast.makeText(activity, "Activating offer on Google Play. Please tap again in a few seconds.", Toast.LENGTH_SHORT).show()
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            updateSubscribedState(true)
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase Acknowledged Successfully with Google Play")
                    } else {
                        Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    }
                }
            }
        }
    }

    private fun updateSubscribedState(subscribed: Boolean) {
        _isSubscribed.value = subscribed
        sharedPrefs.edit().putBoolean("is_subscribed", subscribed).apply()
        try {
            SpamBlockerApp.instance.spamRepository.setSubscribed(subscribed)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync subscription state to repository", e)
        }
    }
}
