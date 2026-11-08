/*
 * Unlock premium and remove ads for com.delphicoder.flud (Flud - Torrent Downloader).
 *
 * Targets (from analyzed smali of com.delphicoder.flud v2.2.7):
 * - Lda0;->onQueryPurchasesResponse(BillingResult, List)V: billing callback that
 *   drives fa0 premium state. Patched to always set premium as owned/settled
 *   (fa0.m = Boolean.FALSE, matching the PURCHASED-settled state) and emit the
 *   premium success event (Ltj4.a) on fa0.k, then return.
 * - Lei3; ad manager: c()Z forced false (ads disabled) and a/b/d()V no-op to
 *   prevent ad initialization, loading and display.
 *
 * Fingerprints use defining class + name + access/return/params to stay resilient
 * across minor builds; verify against target APK smali if matching fails.
 */
package app.morphe.patches.flud.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.flud.premium.Constants

@Suppress("unused")
val unlockFludPremiumPatch = bytecodePatch(
    name = "Unlock Premium + Remove Ads",
    description = "Unlocks premium features and removes all ads in Flud (com.delphicoder.flud) by forcing billing premium state and disabling the ad manager.",
) {
    compatibleWith(Constants.COMPATIBILITY_FLUD)

    execute {
        // 1) Force premium unlocked in billing query response.
        // da0.b holds Lfa0; in onQueryPurchasesResponse (virtual: p0=this, p1=result, p2=list).
        // .locals 5 in target, so v0-v4 are safe to use.
        QueryPurchasesResponseFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lda0;->b:Ljava/lang/Object;
                instance-of v1, v0, Lfa0;
                if-eqz v1, :unlock_skip
                check-cast v0, Lfa0;
                iget-object v1, v0, Lfa0;->m:Lfn5;
                sget-object v2, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                const/4 v3, 0x0
                invoke-virtual {v1, v3, v2}, Lfn5;->k(Ljava/lang/Object;Ljava/lang/Object;)Z
                iget-object v1, v0, Lfa0;->k:Lid0;
                sget-object v2, Ltj4;->a:Ltj4;
                invoke-interface {v1, v2}, Ld95;->g(Ljava/lang/Object;)Ljava/lang/Object;
                :unlock_skip
                return-void
            """,
        )

        // 2) Disable ads - ad manager gate and lifecycle methods.
        AdsEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
        AdsInitFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )
        AdsLoadFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )
        AdsShowFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )
    }
}
