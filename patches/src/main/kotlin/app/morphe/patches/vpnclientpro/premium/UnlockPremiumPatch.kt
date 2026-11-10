/*
 * VPN Client Pro (it.colucciweb.vpnclientpro) - unlock selected pro features.
 *
 * Analysis source: app-source/vpnclientpro_source (decompiled Java, m00, v1.02.57).
 *
 * Targets:
 * - Lyj;->n(I)Z : central feature-license check. Returns true if the given feature ID is in
 *   the licensed set. Feature IDs (from yj.j): 718849192 obfs/scramble, 719849728 TAP emulator,
 *   752704424 auto-connect, 777074589 connect on demand, 823778945 advanced split DNS,
 *   946545622 failover profiles, 1122572344 Tasker/Automate, 1172297389 port knocking,
 *   1204386766 on-connect action, 1257343314 shortcuts, 1290565896 widgets, 1444998748 quick tiles.
 *   Patched to always return true so ex0.b() (requireFeature gate) unlocks these features.
 * - Ltn;->f()Z : auto-connect ViewModel gate (checked directly in UI; error_disabled_feature).
 *   Patched to always return true.
 */
package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.vpnclientpro.premium.Constants

@Suppress("unused")
val unlockVpnClientProFeaturesPatch = bytecodePatch(
    name = "Unlock Pro Features",
    description = "Unlocks licensed features in VPN Client Pro (it.colucciweb.vpnclientpro): obfs/scramble, TAP emulator, auto-connect, connect on demand, split DNS, failover, Tasker/Automate, port knocking, on-connect action, shortcuts, widgets, quick tiles.",
) {
    compatibleWith(Constants.COMPATIBILITY_VPNCLIENTPRO)

    execute {
        fun boolTrue(fp: Fingerprint, label: String, required: Boolean = false): Boolean {
            val ok = runCatching {
                fp.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }.isSuccess
            if (required && !ok) {
                throw PatchException("Could not patch $label. Verify APK is it.colucciweb.vpnclientpro (analyzed v1.02.57) or adjust fingerprints.")
            }
            return ok
        }

        // Central feature license gate used by ex0.b() for requireFeature menu items.
        boolTrue(FeatureLicensedFingerprint, "feature license check (yj.n)", required = true)

        // Auto-connect UI gate (checked directly).
        boolTrue(AutoConnectFeatureGateFingerprint, "auto-connect gate (tn.f)")
    }
}
