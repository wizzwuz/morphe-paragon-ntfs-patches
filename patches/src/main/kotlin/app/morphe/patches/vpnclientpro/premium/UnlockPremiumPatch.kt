/*
 * VPN Client Pro (it.colucciweb.vpnclientpro) - unlock premium features.
 *
 * Analysis source: app-source/vpnclientpro_source (decompiled Java, m00).
 *
 * Targets:
 * - Lls;->m()Ljava/util/List; : returns owned licenses (List<is>) from billing state p.
 *   Patched to synthesize licenses from available products (s map / o SKU list) when the
 *   real purchase list is empty, so license checks see a non-empty, valid-looking list.
 * - Ltn;->f()Z : auto-connect feature gate (shows R.string.error_disabled_feature when false).
 *   Patched to always return true.
 *
 * The app is heavily obfuscated with encrypted strings; fingerprints use defining class/method
 * signatures from the analyzed build (v1.02.57 / 30010257).
 */
package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.vpnclientpro.premium.Constants

@Suppress("unused")
val unlockVpnClientProPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in VPN Client Pro (it.colucciweb.vpnclientpro) by spoofing license state and feature gates.",
) {
    compatibleWith(Constants.COMPATIBILITY_VPNCLIENTPRO)

    execute {
        fun boolTrue(fp: Fingerprint) = runCatching {
            fp.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }.isSuccess

        // 1) Auto-connect feature gate: tn.f() -> true
        boolTrue(AutoConnectFeatureGateFingerprint)

        // 2) License list: if empty, synthesize licenses from known products.
        //    ls fields: p (gt2/state holding List<is>), s (gt2/state holding Map<String,f03>).
        //    Registers v0-v5 (registerCount ensured >= 6).
        runCatching {
            val m = LicenseListFingerprint.method
            m.implementation?.let { impl ->
                if (impl.registerCount < 7) impl.registerCount = 7
            }
            m.addInstructions(
                0,
                """
                    # Get current license list from p
                    iget-object v0, p0, Lls;->p:Lgt2;
                    invoke-interface {v0}, Lgt2;->getValue()Ljava/lang/Object;
                    move-result-object v0
                    check-cast v0, Ljava/util/List;
                    if-eqz v0, :vpn_build
                    invoke-interface {v0}, Ljava/util/List;->isEmpty()Z
                    move-result v1
                    if-nez v1, :vpn_build
                    return-object v0

                    :vpn_build
                    new-instance v0, Ljava/util/ArrayList;
                    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
                    iget-object v1, p0, Lls;->s:Lgt2;
                    if-nez v1, :vpn_have_s
                    return-object v0
                    :vpn_have_s
                    invoke-interface {v1}, Lgt2;->getValue()Ljava/lang/Object;
                    move-result-object v1
                    check-cast v1, Ljava/util/Map;
                    if-eqz v1, :vpn_done
                    invoke-interface {v1}, Ljava/util/Map;->isEmpty()Z
                    move-result v2
                    if-nez v2, :vpn_done

                    invoke-interface {v1}, Ljava/util/Map;->values()Ljava/util/Collection;
                    move-result-object v1
                    invoke-interface {v1}, Ljava/util/Collection;->iterator()Ljava/util/Iterator;
                    move-result-object v1

                    :vpn_loop
                    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z
                    move-result v2
                    if-nez v2, :vpn_done

                    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
                    move-result-object v2
                    check-cast v2, Lf03;
                    if-nez v2, :vpn_loop

                    # v3 = sku string (f03.b) or fallback "premium"
                    iget-object v3, v2, Lf03;->b:Ljava/lang/String;
                    if-eqz v3, :vpn_sku_fallback
                    goto :vpn_sku_ready
                    :vpn_sku_fallback
                    const-string v3, "premium"
                    :vpn_sku_ready

                    # v4 = StringBuilder for JSON
                    new-instance v4, Ljava/lang/StringBuilder;
                    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V
                    const-string v5, "{\"productId\":\""
                    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                    move-result-object v4
                    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                    move-result-object v4
                    const-string v5, "\",\"purchaseToken\":\"spoofed\",\"purchaseState\":1,\"acknowledged\":true}"
                    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                    move-result-object v4
                    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                    move-result-object v3

                    # v4 = new m13(json, sig)
                    new-instance v4, Lm13;
                    const-string v5, "spoofed_signature"
                    invoke-direct {v4, v3, v5}, Lm13;-><init>(Ljava/lang/String;Ljava/lang/String;)V

                    # v3 = new is(m13, f03)
                    new-instance v3, Lis;
                    invoke-direct {v3, v4, v2}, Lis;-><init>(Lm13;Lf03;)V
                    invoke-virtual {v0, v3}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
                    goto :vpn_loop

                    :vpn_done
                    return-object v0
                """
            )
        }.onFailure {
            throw PatchException("Could not patch VPN Client Pro license list (ls.m). Verify APK version or adjust fingerprints.")
        }
    }
}
