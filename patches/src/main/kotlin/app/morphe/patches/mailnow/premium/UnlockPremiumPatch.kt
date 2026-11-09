/*
 * Mail Now (air.kukulive.mailnow) - remove ads and spoof premium purchase callbacks.
 *
 * Analysis source: app-source/mailnow_source (decompiled smali, m00).
 *
 * Ad removal (app-specific + SDK fallbacks):
 * - Lair/kukulive/mailnow/MainActivity;->V()V: consent gate that launches banner ad loading.
 * - Lu11;->run()V: builds AdRequest and loads the banner AdView.
 * - Lz11;->a()V: MobileAds initialization-complete callback that starts the ad chain.
 * - Bundled AdMob SDK hooks (obfuscated + standard): BaseAdView load, MobileAds init,
 *   AdLoader.loadAd, InterstitialAd.load/show.
 *
 * Premium (client-side spoof): the app is a web wrapper; premium is handled by
 * smphone.app.premium.php and native billing reports results to JS via
 * buyAppItem('successPurchase', token, productId). MainActivity.Q(action, skuCsv) is the
 * web->native billing entry (FlexCall->pz2). This patch stores L/M then spoofs a
 * successPurchase callback for buy/restore/restore_silent actions using the requested SKU.
 * NOTE: if the server verifies purchase tokens with Google Play, server-side entitlement
 * cannot be granted by a client-only patch; this covers local/UI callback gating.
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
    description = "Removes AdMob ads and spoofs premium purchase/restore callbacks in Mail Now (air.kukulive.mailnow). Client-side; server token verification may still limit premium.",
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

        // Client-side premium spoof: Q(action, skuCsv) is called by web flow.
        // Uses only v0 to stay within the method's original register frame (.locals 1).
        runCatching {
            BillingEntryFingerprint.method.addInstructions(
                0,
                """
                    # Store billing action/SKU as original method does.
                    iput-object p1, p0, Lair/kukulive/mailnow/MainActivity;->L:Ljava/lang/String;
                    iput-object p2, p0, Lair/kukulive/mailnow/MainActivity;->M:Ljava/lang/String;

                    # Spoof for buy/restore/restore_silent.
                    const-string v0, "buy"
                    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :mn_check_restore
                    goto :mn_spoof

                    :mn_check_restore
                    const-string v0, "restore"
                    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :mn_check_restore_silent
                    goto :mn_spoof

                    :mn_check_restore_silent
                    const-string v0, "restore_silent"
                    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :mn_spoof

                    # Not a buy/restore action we spoof: skip real billing setup.
                    return-void

                    :mn_spoof
                    const-string v0, "javascript:buyAppItem('successPurchase','morphe_spoofed_token','premium_year');"
                    invoke-virtual {p0, v0}, Lair/kukulive/mailnow/MainActivity;->J(Ljava/lang/String;)V
                    return-void
                """
            )
        }

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
