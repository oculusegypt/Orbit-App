package com.example.ui.viewmodel


import android.content.Context
import androidx.compose.runtime.Immutable
import com.example.core.OrbitCouponEngine
import com.example.core.OrbitLedger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Immutable snapshot of the checkout pricing after real coupon validation. */
@Immutable
data class CheckoutPricing(
    val basePriceMinor: Int,
    val finalPriceMinor: Int,
    val appliedCampaignId: String? = null,
    val discountPercent: Int = 0,
) {
    val hasDiscount: Boolean get() = appliedCampaignId != null && discountPercent > 0
}

/** Persisted receipt for a completed subscription purchase. */
@Immutable
data class PurchaseReceipt(
    val orderId: String,
    val userId: String,
    val courseId: String,
    val courseTitle: String,
    val paidAmountMinor: Int,
    val currency: String,
    val paymentMethod: String,
    val campaignId: String?,
    val discountPercent: Int,
    val referenceCode: String,
    val createdAt: String,
)

/**
 * Owns the checkout funnel: real signed-coupon validation against expiry and
 * per-user quota, price computation in minor units, and durable receipts.
 * All state is persisted through [OrbitLedger] (SharedPreferences-backed) AND
 * mirrored into Room via [roomReceiptDaoProvider], so discounts cannot be
 * re-redeemed across app restarts and revenue KPIs are queryable offline.
 */
class CheckoutController(
    private val ledgerProvider: () -> OrbitLedger?,
    private val roomReceiptDaoProvider: () -> com.example.data.local.PurchaseReceiptDao? = { null },
    private val scopeProvider: () -> kotlinx.coroutines.CoroutineScope? = { null },
) {

    private val _pricing = MutableStateFlow<CheckoutPricing?>(null)
    val pricing: StateFlow<CheckoutPricing?> = _pricing.asStateFlow()

    private val _couponFeedback = MutableStateFlow<String?>(null)
    val couponFeedback: StateFlow<String?> = _couponFeedback.asStateFlow()

    private val _lastReceipt = MutableStateFlow<PurchaseReceipt?>(null)
    val lastReceipt: StateFlow<PurchaseReceipt?> = _lastReceipt.asStateFlow()

    /** Called when the dialog opens for [course] — resets any stale quote. */
    fun openForCourse(basePriceMinor: Int) {
        _pricing.value = CheckoutPricing(basePriceMinor = basePriceMinor, finalPriceMinor = basePriceMinor)
        _couponFeedback.value = null
    }

    fun close() {
        _pricing.value = null
        _couponFeedback.value = null
    }

    /**
     * Validates [code] for [userId] with the real engine (signature + expiry +
     * quota). On success recomputes the quote; otherwise exposes an Arabic
     * reason through [couponFeedback].
     */
    fun applyCoupon(rawCode: String, userId: String, basePriceMinor: Int) {
        val ledger = ledgerProvider()
        val used = ledger?.stringSet(LEDGER_REDEEMED) ?: emptySet()
        when (val result = OrbitCouponEngine.validate(rawCode, userId, used)) {
            is OrbitCouponEngine.RedemptionResult.Success -> {
                val discounted = OrbitCouponEngine.applyDiscount(basePriceMinor, result.campaign)
                _pricing.value = CheckoutPricing(
                    basePriceMinor = basePriceMinor,
                    finalPriceMinor = discounted,
                    appliedCampaignId = result.campaign.id,
                    discountPercent = result.campaign.discountPercent,
                )
                _couponFeedback.value = "تم تفعيل خصم ${result.campaign.discountPercent}% ✓"
            }
            OrbitCouponEngine.RedemptionResult.UnknownCode ->
                _couponFeedback.value = "كود الخصم غير معروف أو منتهي الصلاحية"
            OrbitCouponEngine.RedemptionResult.Expired ->
                _couponFeedback.value = "انتهت صلاحية هذا الكود"
            OrbitCouponEngine.RedemptionResult.AlreadyUsed ->
                _couponFeedback.value = "سبق استخدام هذا الكود على حسابك"
            OrbitCouponEngine.RedemptionResult.InvalidSignature ->
                _couponFeedback.value = "كود غير صالح (لم يتم التحقق من توقيعه)"
        }
    }

    /**
     * Commits a purchase: writes the redemption into the persistent ledger and
     * produces a durable receipt. Returns the receipt or null when no quote is open.
     */
    fun commitPurchase(
        userId: String,
        courseId: String,
        courseTitle: String,
        currency: String,
        paymentMethod: String,
    ): PurchaseReceipt? {
        val quote = _pricing.value ?: return null
        val ledger = ledgerProvider()
        if (ledger != null && quote.appliedCampaignId != null) {
            val used = ledger.stringSet(LEDGER_REDEEMED).toMutableSet()
            used += "$userId::${quote.appliedCampaignId}"
            ledger.putStringSet(LEDGER_REDEEMED, used)
        }

        val receipt = PurchaseReceipt(
            orderId = "ord_" + java.util.UUID.randomUUID().toString().take(8),
            userId = userId,
            courseId = courseId,
            courseTitle = courseTitle,
            paidAmountMinor = quote.finalPriceMinor,
            currency = currency,
            paymentMethod = paymentMethod,
            campaignId = quote.appliedCampaignId,
            discountPercent = quote.discountPercent,
            referenceCode = OrbitCouponEngine.sign(courseId.takeLast(6), userId).take(12).uppercase(),
            createdAt = com.example.core.OrbitTimeFormatter.currentDateTime(),
        )
        persistReceipt(ledger, receipt)
        persistReceiptToRoom(receipt)
        _lastReceipt.value = receipt
        return receipt
    }

    /** Mirrors the receipt into Room so revenue KPIs are durable & queryable. */
    private fun persistReceiptToRoom(receipt: PurchaseReceipt) {
        val dao = roomReceiptDaoProvider() ?: return
        scopeProvider()?.launch(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertReceipt(
                com.example.data.local.PurchaseReceiptEntity(
                    orderId = receipt.orderId,
                    userId = receipt.userId,
                    courseId = receipt.courseId,
                    courseTitle = receipt.courseTitle,
                    paidAmountMinor = receipt.paidAmountMinor,
                    currency = receipt.currency,
                    paymentMethod = receipt.paymentMethod,
                    campaignId = receipt.campaignId,
                    discountPercent = receipt.discountPercent,
                    referenceCode = receipt.referenceCode,
                    createdAt = receipt.createdAt,
                )
            )
        }
    }

    fun currentQuote(): CheckoutPricing? = _pricing.value

    private fun persistReceipt(ledger: OrbitLedger?, receipt: PurchaseReceipt) {
        if (ledger == null) return
        val existing = ledger.stringSet(LEDGER_RECEIPTS).toMutableSet()
        existing += listOf(
            "${receipt.orderId}|${receipt.userId}|${receipt.courseId}|${receipt.paidAmountMinor}" +
                "|${receipt.currency}|${receipt.paymentMethod}|${receipt.discountPercent}|${receipt.createdAt}"
        )
        ledger.putStringSet(LEDGER_RECEIPTS, existing)
    }

    private companion object {
        const val LEDGER_REDEEMED = "redeemed_coupons"
        const val LEDGER_RECEIPTS = "purchase_receipts"
    }
}

/** SharedPreferences implementation of [OrbitLedger]; kept off the controllers. */
class SharedPrefsLedger(context: Context) : OrbitLedger {
    private val prefs = context.applicationContext
        .getSharedPreferences("orbit_checkout_ledger", Context.MODE_PRIVATE)

    override fun stringSet(key: String): Set<String> =
        prefs.getStringSet(key, emptySet()) ?: emptySet()

    override fun putStringSet(key: String, values: Set<String>) {
        prefs.edit().putStringSet(key, values).apply()
    }
}
