package app.morphe.patches.flud.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Flud v2.2.7 billing callback (obfuscated class da0).
 * Stable API method name/signature from BillingClient; defining class pinned for v2.2.7.
 */
internal object QueryPurchasesResponseFingerprint : Fingerprint(
    definingClass = "Lda0;",
    name = "onQueryPurchasesResponse",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
)

/** Flexible fallbacks for other Flud builds / callback paths. */
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
 * v2.2.7-era obfuscated ad manager (ei3). Best-effort on other versions.
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
 * String-based; best-effort (may not resolve on all builds/localizations).
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
        string("premium"),
        string("unlocked"),
        opcode(Opcode.RETURN_VOID),
    ),
)

/**
 * Bypass storage permission setup/onboarding blocks (MANAGE_EXTERNAL_STORAGE / all-files flow).
 */
internal object StoragePermissionSetupFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION"),
    ),
)

internal object ManageExternalStorageCheckFingerprint : Fingerprint(
    returnType = "Z",
    filters = listOf(
        string("android.permission.MANAGE_EXTERNAL_STORAGE"),
        string("isExternalStorageManager"),
    ),
)

/**
 * Google Play Integrity / licensing verdict handling (best-effort).
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
 * AdMob banner/interstitial load entry points (best-effort).
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
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/google/android/gms/ads/initialization/OnInitializationCompleteListener;",
    ),
)

internal object AdLoaderLoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/AdLoader;",
    name = "loadAd",
    returnType = "V",
)

internal object InterstitialShowFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
    name = "show",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;"),
)
