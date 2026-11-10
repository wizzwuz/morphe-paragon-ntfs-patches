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

        // 2) License list: if empty, try to synthesize licenses from product map s.
        //    ls fields: p (gt2/state -> List<is>), s (gt2/state -> Map<String,f03>).
        //    Requires extra registers; falls back gracefully if synthesis fails.
        runCatching {
            val method = LicenseListFingerprint.method
            val impl = method.implementation
            if (impl != null && impl.registerCount < 8) {
                try {
                    val regField = impl.javaClass.getMethod("setRegisterCount", Int::class.javaPrimitiveType)
                    regField.invoke(impl, 8)
                } catch (_: Throwable) {
                    try {
                        val f = impl.javaClass.getDeclaredField("registerCount")
                        f.isAccessible = true
                        f.setInt(impl, 8)
                    } catch (_: Throwable) {
                    }
                }
            }
            method.addInstructions(
                0,
                """
                    # Current license list from p
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
                    if-nez v1, :vpn_done
                    invoke-interface {v1}, Lgt2;->getValue()Ljava/lang/Object;
                    move-result-object v1
                    check-cast v1, Ljava/util/Map;
                    if-eqz v1, :vpn_done
                    invoke-interface {v1}, Ljava/util/Map;->isEmpty()Z
                    move-result v2
                    if-eqz v2, :vpn_done

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

                    # v3 = json, v4 = sig, v5 = m13, v6 = is
                    const-string v3, "{\"productId\":\"premium\",\"purchaseToken\":\"spoofed\",\"purchaseState\":1,\"acknowledged\":true}"
                    const-string v4, "spoofed_signature"
                    new-instance v5, Lm13;
                    invoke-direct {v5, v3, v4}, Lm13;-><init>(Ljava/lang/String;Ljava/lang/String;)V
                    new-instance v6, Lis;
                    invoke-direct {v6, v5, v2}, Lis;-><init>(Lm13;Lf03;)V
                    invoke-virtual {v0, v6}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
                    goto :vpn_loop

                    :vpn_done
                    return-object v0
                """
            )
        }
    }
}
