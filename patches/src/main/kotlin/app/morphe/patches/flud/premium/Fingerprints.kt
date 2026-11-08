package app.morphe.patches.flud.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Billing purchases query callback (obfuscated class da0).
 * Forcing this to report a valid premium purchase unlocks premium features
 * without Google Play Billing.
 */
internal object QueryPurchasesResponseFingerprint : Fingerprint(
    definingClass = "Lda0;",
    name = "onQueryPurchasesResponse",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
)

/**
 * Ad manager (obfuscated class ei3) - "can show / ads enabled" gate.
 */
internal object AdsEnabledFingerprint : Fingerprint(
    definingClass = "Lei3;",
    name = "c",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

internal object AdsLoadFingerprint : Fingerprint(
    definingClass = "Lei3;",
    name = "b",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)

internal object AdsInitFingerprint : Fingerprint(
    definingClass = "Lei3;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)

internal object AdsShowFingerprint : Fingerprint(
    definingClass = "Lei3;",
    name = "d",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)
