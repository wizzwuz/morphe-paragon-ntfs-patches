package app.morphe.patches.flud.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Google Play Billing callbacks. Method names/signatures are stable across app
 * obfuscation because they implement the Billing client callback API.
 * Matched broadly (no pinned obfuscated defining class) so it works across Flud versions.
 */
internal object QueryPurchasesResponseFingerprint : Fingerprint(
    name = "onQueryPurchasesResponse",
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
)

internal object PurchasesUpdatedFingerprint : Fingerprint(
    name = "onPurchasesUpdated",
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
)

internal object PurchaseHistoryResponseFingerprint : Fingerprint(
    name = "onPurchaseHistoryResponse",
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
)

/**
 * v2.2.7-era obfuscated ad manager (ei3). Kept for compatibility with that build.
 * If absent on other versions, the patch still applies the generic ad/billing fixes below.
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

/**
 * Removes the "Thanks! premium is now unlocked" popup/toast/dialog.
 * Matches methods containing the distinctive unlocked-premium message text.
 */
internal object PremiumUnlockedPopupFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("premium is now unlocked"),
    ),
)

internal object PremiumUnlockedPopupAltFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("Thanks"),
        string("premium"),
        string("unlocked"),
    ),
)

/**
 * Bypass storage permission setup/onboarding blocks that prevent using the app until
 * all-files/storage access is granted (common MANAGE_EXTERNAL_STORAGE flow).
 */
internal object StoragePermissionSetupFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION"),
    ),
)

/**
 * Fallback storage gate: methods that check/route around MANAGE_EXTERNAL_STORAGE.
 */
internal object ManageExternalStorageCheckFingerprint : Fingerprint(
    returnType = "Z",
    filters = listOf(
        string("android.permission.MANAGE_EXTERNAL_STORAGE"),
    ),
)

/**
 * Google Play Integrity / licensing verdict handling. Returning a successful/allowed
 * result prevents integrity-gated premium/license blocks.
 */
internal object PlayIntegrityResultFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Lcom/google/android/play/core/integrity/IntegrityTokenResponse;",
            name = "token",
            returnType = "Ljava/lang/String;",
        ),
    ),
)

/**
 * AdMob banner/interstitial load entry points - prevents banner views from loading ads.
 */
internal object AdViewLoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/BaseAdView;",
    name = "loadAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/AdRequest;"),
)

internal object InterstitialLoadFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
    name = "load",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Lcom/google/android/gms/ads/AdRequest;",
        "Lcom/google/android/gms/ads/interstitial/InterstitialAdLoadCallback;",
    ),
)

internal object MobileAdsInitializeFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAds;",
    name = "initialize",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
)
