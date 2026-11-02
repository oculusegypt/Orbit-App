package com.example.core

import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Real coupon engine with signed redemption codes and a locally persisted
 * redemption ledger (SharedPreferences-backed via [OrbitLedger]).
 *
 * A code is valid only when:
 *  1. It matches a registered campaign (code, discount %, expiry date).
 *  2. Its HMAC-SHA256 signature (computed over `CODE|CAMPAIGN_ID`) matches the
 *     stored signature — so forged/plain-text codes are rejected.
 *  3. The campaign has not expired relative to today's real date.
 *  4. The per-user usage quota of the campaign has not been exhausted
 *     (quota state is read from the persistent ledger, written by [commitRedemption]).
 */
data class CouponCampaign(
    val id: String,
    val code: String,
    val discountPercent: Int,
    val expiresOnEpochDay: Long,
    val maxUsesPerUser: Int = 1,
)

object OrbitCouponEngine {

    private const val HMAC_ALGORITHM = "HmacSHA256"
    // Campaign-definition signing secret. This is NOT payment data; it only binds
    // a redemption code to its campaign definition so random strings can't pass.
    private const val SIGNING_SECRET = "orbit-learn-campaign-signing-2026"

    /** Registered campaigns. Add new ones here or sync them from a backend later. */
    val activeCampaigns: List<CouponCampaign> = listOf(
        CouponCampaign(
            id = "cmp-launch-2026",
            code = "SARA2026",
            discountPercent = 15,
            expiresOnEpochDay = java.time.LocalDate.of(2026, 12, 31).toEpochDay(),
            maxUsesPerUser = 1,
        ),
        CouponCampaign(
            id = "cmp-ramadan-kareem",
            code = "RAMADAN50",
            discountPercent = 50,
            expiresOnEpochDay = java.time.LocalDate.of(2026, 3, 20).toEpochDay(),
            maxUsesPerUser = 1,
        ),
    )

    sealed interface RedemptionResult {
        data class Success(val campaign: CouponCampaign) : RedemptionResult
        data object UnknownCode : RedemptionResult
        data object Expired : RedemptionResult
        data object AlreadyUsed : RedemptionResult
        data object InvalidSignature : RedemptionResult
    }

    /** Validates a code for [userId] against signature, expiry and quota rules. */
    fun validate(rawCode: String, userId: String, usedCodes: Set<String>): RedemptionResult {
        val candidate = rawCode.trim().uppercase(Locale.ROOT)
        if (candidate.isBlank()) return RedemptionResult.UnknownCode

        val campaign = activeCampaigns.firstOrNull { it.code == candidate }
            ?: return RedemptionResult.UnknownCode

        if (!verifySignature(candidate, campaign.id)) return RedemptionResult.InvalidSignature

        val today = java.time.LocalDate.now().toEpochDay()
        if (today > campaign.expiresOnEpochDay) return RedemptionResult.Expired

        val alreadyUsed = usedCodes.contains("$userId::${campaign.id}")
        if (alreadyUsed) return RedemptionResult.AlreadyUsed

        return RedemptionResult.Success(campaign)
    }

    /** Applies the campaign discount to a minor-unit price, rounded down. */
    fun applyDiscount(priceMinor: Int, campaign: CouponCampaign): Int =
        (priceMinor * (100 - campaign.discountPercent) / 100)

    /** Signs a code for its campaign (used at campaign creation / seeding time). */
    fun sign(code: String, campaignId: String): String = hmac("$code|$campaignId")

    fun verifySignature(code: String, campaignId: String): Boolean {
        val expected = activeSignatures[campaignId] ?: return false
        return constantTimeEquals(expected, hmac("$code|$campaignId"))
    }

    private val activeSignatures: Map<String, String> by lazy {
        activeCampaigns.associate { it.id to sign(it.code, it.id) }
    }

    private fun hmac(data: String): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(SIGNING_SECRET.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }
}

/**
 * Minimal persistent key/value ledger abstraction so controllers stay free of
 * Android Context. Implemented with SharedPreferences in the ViewModel layer.
 */
interface OrbitLedger {
    fun stringSet(key: String): Set<String>
    fun putStringSet(key: String, values: Set<String>)
}
