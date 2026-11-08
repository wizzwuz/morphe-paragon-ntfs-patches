/*
 * Flud (com.delphicoder.flud) v2.2.7 - premium unlock, ad removal, popup removal,
 * storage-permission setup bypass, and Play Integrity/licensing bypass.
 *
 * Primary targets (v2.2.7):
 * - Lda0;->onQueryPurchasesResponse(BillingResult,List)V: force premium state
 *   (fa0.m/fn5, fa0.k emits tj4.a) and return before original handling.
 * - Lei3; ad manager: c()Z false, a/b/d()V no-op.
 *
 * Additional best-effort hooks (applied only if resolved): popup string removal,
 * AdMob load/init, storage permission gates, Play Integrity verdict handling.
 */
package app.morphe.patches.flud.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.flud.premium.Constants

@Suppress("unused")
val unlockFludPremiumPatch = bytecodePatch(
    name = "Unlock Premium + Remove Ads",
    description = "Unlocks premium, removes ads/banners/interstitials, removes the premium-unlocked popup, bypasses storage-permission setup, and disables Play Integrity/licensing checks in Flud (com.delphicoder.flud).",
) {
    compatibleWith(Constants.COMPATIBILITY_FLUD)

    execute {
        fun voidAtStart(fp: Fingerprint) = runCatching { fp.method.addInstructions(0, "return-void") }.isSuccess
        fun boolAtStart(fp: Fingerprint, value: Int) = runCatching {
            fp.method.addInstructions(0, "const/4 v0, 0x$value\nreturn v0")
        }.isSuccess

        // 1) Billing purchases callback.
        // v2.2.7-specific state manipulation (safe only for Lda0; layout).
        val v227PremiumStateSmali = """
            iget-object v0, p0, Lda0;->b:Ljava/lang/Object;
            instance-of v1, v0, Lfa0;
            if-eqz v1, :flud_premium_done
            check-cast v0, Lfa0;
            iget-object v1, v0, Lfa0;->m:Lfn5;
            if-eqz v1, :flud_premium_emit
            sget-object v2, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            const/4 v3, 0x0
            invoke-virtual {v1, v3, v2}, Lfn5;->k(Ljava/lang/Object;Ljava/lang/Object;)Z
            :flud_premium_emit
            iget-object v1, v0, Lfa0;->k:Lid0;
            if-eqz v1, :flud_premium_done
            sget-object v2, Ltj4;->a:Ltj4;
            invoke-interface {v1, v2}, Ld95;->g(Ljava/lang/Object;)Ljava/lang/Object;
            :flud_premium_done
            return-void
        """.trimIndent()
        // Generic fallback: block original callback handling (cannot safely assume obfuscated field layout).
        val genericBillingSmali = "return-void"

        val billingPatched =
            runCatching { QueryPurchasesResponseFingerprint.method.addInstructions(0, v227PremiumStateSmali) }.isSuccess ||
                runCatching { PurchasesUpdatedFingerprint.method.addInstructions(0, genericBillingSmali) }.isSuccess ||
                runCatching { PurchaseHistoryResponseFingerprint.method.addInstructions(0, genericBillingSmali) }.isSuccess
        if (!billingPatched) {
            throw PatchException("Could not find Flud billing callback (onQueryPurchasesResponse/onPurchasesUpdated). Verify APK is com.delphicoder.flud v2.2.7 or adjust fingerprints.")
        }

        // 2) Remove "Thanks! premium is now unlocked" popup (best-effort).
        voidAtStart(PremiumUnlockedPopupFingerprint)
        voidAtStart(PremiumUnlockedPopupAltFingerprint)

        // 3) v2.2.7 ad manager (ei3).
        boolAtStart(AdsEnabledFingerprint, 0)
        voidAtStart(AdsInitFingerprint)
        voidAtStart(AdsLoadFingerprint)
        voidAtStart(AdsShowFingerprint)

        // 4) AdMob (best-effort): block banner/interstitial loading and SDK initialization.
        voidAtStart(AdViewLoadAdFingerprint)
        voidAtStart(AdLoaderLoadAdFingerprint)
        voidAtStart(InterstitialLoadFingerprint)
        voidAtStart(InterstitialShowFingerprint)
        voidAtStart(MobileAdsInitializeFingerprint)

        // 5) Bypass storage-permission setup/onboarding blocks (best-effort).
        voidAtStart(StoragePermissionSetupFingerprint)
        boolAtStart(ManageExternalStorageCheckFingerprint, 1)

        // 6) Play Integrity / licensing (best-effort).
        voidAtStart(PlayIntegrityResultFingerprint)
    }
}
