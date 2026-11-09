package app.morphe.patches.mailnow.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Mail Now (air.kukulive.mailnow) ad-loading entry points.
 * Analysis based on decompiled source in app-source/mailnow_source (m00).
 */

/** Consent-gated ad loader: checks consent and runs the banner loader on the UI thread. */
internal object MainAdGateFingerprint : Fingerprint(
    definingClass = "Lair/kukulive/mailnow/MainActivity;",
    name = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)

/** Banner ad loader Runnable: creates AdView, sets AdListener, builds AdRequest, loads ad. */
internal object BannerAdLoaderFingerprint : Fingerprint(
    definingClass = "Lu11;",
    name = "run",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)

/** MobileAds initialization-complete callback; posts the chain that eventually calls MainActivity.V(). */
internal object MobileAdsInitCallbackFingerprint : Fingerprint(
    definingClass = "Lz11;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
)

/** Obfuscated BaseAdView.loadAd(AdRequest) in the bundled AdMob SDK used by this app build. */
internal object BaseAdViewObfLoadFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/BaseAdView;",
    name = "b",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/AdRequest;"),
)

/** Standard BaseAdView.loadAd fallback (non-obfuscated SDK builds). */
internal object BaseAdViewLoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/BaseAdView;",
    name = "loadAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/AdRequest;"),
)

/** Obfuscated MobileAds initialize/init entry observed in MainActivity (MobileAds;->b(Activity, callback)). */
internal object MobileAdsObfInitFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAds;",
    name = "b",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf(
        "Lair/kukulive/mailnow/MainActivity;",
        "Lz11;",
    ),
)

/** Standard MobileAds.initialize fallback. */
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

/** Best-effort: block interstitial ad loading. */
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

/** Best-effort: block interstitial ad showing. */
internal object InterstitialShowFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
    name = "show",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;"),
)

/** Best-effort: block AdLoader.loadAd (native/banner mediation paths). */
internal object AdLoaderLoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/AdLoader;",
    name = "loadAd",
    returnType = "V",
)
