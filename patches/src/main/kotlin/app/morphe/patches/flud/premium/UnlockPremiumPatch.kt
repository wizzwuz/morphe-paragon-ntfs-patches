/*
 * Flud (com.delphicoder.flud) - premium unlock, ad removal, popup removal,
 * storage-permission setup bypass, and Play Integrity/licensing bypass.
 *
 * Strategy:
 * - Hook the stable Billing callback name (onQueryPurchasesResponse) to force
 *   premium handling to stop in the original callback path.
 * - Remove the "premium is now unlocked" popup by no-oping methods that contain
 *   that message.
 * - Disable app-level and AdMob ad loading/display (banners + interstitials).
 * - Bypass storage-permission setup gates and Play Integrity verdict handling.
 *
 * Fingerprints that depend on SDK/app internals are applied best-effort so one
 * missing symbol on a given APK version does not abort the whole patch.
 */
package app.morphe.patches.flud.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
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

        // 1) Billing purchases callbacks (stable API names/signatures).
        val premiumStateSmali = """
            # Best-effort v2.2.7 premium state (da0.b -> fa0, fa0.m/fn5, fa0.k emits tj4.a)
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
        """
        val billingPatched =
            runCatching { QueryPurchasesResponseFingerprint.method.addInstructions(0, premiumStateSmali) }.isSuccess ||
                runCatching { PurchasesUpdatedFingerprint.method.addInstructions(0, premiumStateSmali) }.isSuccess ||
                runCatching { PurchaseHistoryResponseFingerprint.method.addInstructions(0, premiumStateSmali) }.isSuccess
        // Critical path: premium unlock depends on billing callbacks matching.
        if (!billingPatched) {
            QueryPurchasesResponseFingerprint.method.addInstructions(0, premiumStateSmali)
        }

        // 2) Remove "Thanks! premium is now unlocked" popup.
        voidAtStart(PremiumUnlockedPopupFingerprint)
        voidAtStart(PremiumUnlockedPopupAltFingerprint)

        // 3) Legacy v2.2.7 ad manager (ei3) - best effort.
        boolAtStart(AdsEnabledFingerprint, 0)
        voidAtStart(AdsInitFingerprint)
        voidAtStart(AdsLoadFingerprint)
        voidAtStart(AdsShowFingerprint)

        // 4) AdMob: block banner/interstitial loading and SDK initialization.
        voidAtStart(AdViewLoadAdFingerprint)
        voidAtStart(InterstitialLoadFingerprint)
        voidAtStart(MobileAdsInitializeFingerprint)

        // 5) Bypass storage-permission setup/onboarding blocks.
        voidAtStart(StoragePermissionSetupFingerprint)
        boolAtStart(ManageExternalStorageCheckFingerprint, 1)

        // 6) Play Integrity / licensing: avoid failure paths in verdict handling.
        voidAtStart(PlayIntegrityResultFingerprint)
    }
}
