/*
 * Mail Now (air.kukulive.mailnow) - remove ads and unlock premium UI where locally gated.
 *
 * Analysis source: app-source/mailnow_source (decompiled smali, m00).
 *
 * Ad removal (high confidence, app-specific + SDK fallbacks):
 * - Lair/kukulive/mailnow/MainActivity;->V()V: consent gate that launches banner ad loading.
 * - Lu11;->run()V: builds AdRequest and calls BaseAdView.loadAd (obfuscated as b).
 * - Lz11;->a()V: MobileAds initialization-complete callback that starts the ad chain.
 * - Lcom/google/android/gms/ads/BaseAdView;->b(AdRequest)V: obfuscated banner load in this build.
 * - Lcom/google/android/gms/ads/MobileAds;->b(MainActivity;Lz11;)V: obfuscated init in this build.
 * - Standard/fallback AdMob entry points (loadAd, initialize, interstitial load/show, AdLoader).
 *
 * Premium: no local isPremium/remove-ads boolean was found in smali; premium appears to be
 * handled via server/web pages (smphone.app.premium.php). This patch removes ad loading and
 * blocks AdMob entry points; server-side premium entitlements cannot be granted locally.
 */
package app.morphe.patches.mailnow.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.mailnow.premium.Constants

@Suppress("unused")
val unlockMailNowPremiumPatch = bytecodePatch(
    name = "Unlock Premium + Remove Ads",
    description = "Removes AdMob ads/banners/interstitials and blocks ad initialization in Mail Now (air.kukulive.mailnow). Premium features appear server-side; local ad loading is disabled.",
) {
    compatibleWith(Constants.COMPATIBILITY_MAILNOW)

    execute {
        fun voidAtStart(fp: Fingerprint) = runCatching { fp.method.addInstructions(0, "return-void") }.isSuccess

        // Required app-specific hooks: fail loudly if the main ad gate is missing.
        val gatePatched = voidAtStart(MainAdGateFingerprint)
        if (!gatePatched) {
            throw PatchException("Could not patch Mail Now ad gate (MainActivity.V). Verify APK is air.kukulive.mailnow or adjust fingerprints.")
        }

        // App-specific ad chain hooks (best-effort but expected for this build).
        voidAtStart(BannerAdLoaderFingerprint)
        voidAtStart(MobileAdsInitCallbackFingerprint)

        // Bundled SDK hooks (obfuscated names observed in this build + standard fallbacks).
        voidAtStart(BaseAdViewObfLoadFingerprint)
        voidAtStart(BaseAdViewLoadAdFingerprint)
        voidAtStart(MobileAdsObfInitFingerprint)
        voidAtStart(MobileAdsInitializeFingerprint)
        voidAtStart(AdLoaderLoadAdFingerprint)
        voidAtStart(InterstitialLoadFingerprint)
        voidAtStart(InterstitialShowFingerprint)
    }
}
