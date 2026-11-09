/*
 * Mail Now (air.kukulive.mailnow) - remove ads.
 *
 * Analysis source: app-source/mailnow_source (decompiled smali, m00).
 *
 * Ad removal (app-specific + SDK fallbacks):
 * - Lair/kukulive/mailnow/MainActivity;->V()V: consent gate that launches banner ad loading.
 * - Lu11;->run()V: builds AdRequest and loads the banner AdView.
 * - Lz11;->a()V: MobileAds initialization-complete callback that starts the ad chain.
 * - Bundled AdMob SDK hooks (obfuscated + standard): BaseAdView load, MobileAds init,
 *   AdLoader.loadAd, InterstitialAd.load/show.
 */
package app.morphe.patches.mailnow.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.mailnow.premium.Constants

@Suppress("unused")
val removeMailNowAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes AdMob ads/banners/interstitials and blocks ad initialization in Mail Now (air.kukulive.mailnow).",
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
