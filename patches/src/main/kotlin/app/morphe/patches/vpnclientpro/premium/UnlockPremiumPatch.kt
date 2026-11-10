/*
 * VPN Client Pro (it.colucciweb.vpnclientpro) - unlock selected pro features.
 *
 * Analysis source: app-source/vpnclientpro_source (decompiled Java, m00, v1.02.57).
 *
 * Targets (feature/license gates only):
 * - Lyj;->n(I)Z : central feature-license check (used by ex0.b() requireFeature items).
 * - Lyj;->o(Lyj;I;Loc0;)Ljava/lang/Object; : async feature authorization (widgets/tiles/runtime).
 * - Ltn;->f()Z : auto-connect UI gate.
 *
 * Unlocked feature IDs (from yj.j): protocols + requested features
 *   19339902 OpenVPN, 166729092 SSTP, 238425325 OpenConnect, 478614834 WireGuard,
 *   536074627 SoftEther, 551267005 Shadowsocks, 608107152 SSH,
 *   718849192 obfs/scramble, 719849728 TAP emulator, 752704424 auto-connect,
 *   777074589 connect on demand, 823778945 advanced split DNS, 946545622 failover,
 *   1122572344 Tasker/Automate, 1172297389 port knocking, 1204386766 on-connect action,
 *   1257343314 shortcuts, 1290565896 widgets, 1444998748 quick tiles.
 */
package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.vpnclientpro.premium.Constants

@Suppress("unused")
val unlockVpnClientProFeaturesPatch = bytecodePatch(
    name = "Unlock Pro Features",
    description = "Unlocks selected features/protocols in VPN Client Pro (it.colucciweb.vpnclientpro): all protocols, obfs/scramble, TAP emulator, auto-connect, connect on demand, split DNS, failover, Tasker/Automate, port knocking, on-connect action, shortcuts, widgets, quick tiles.",
) {
    compatibleWith(Constants.COMPATIBILITY_VPNCLIENTPRO)

    execute {
        // Feature IDs (from yj.j, v1.02.57).
        val featureIds = listOf(
            19339902,   // OpenVPN client
            166729092,  // SSTP client
            238425325,  // OpenConnect client
            478614834,  // WireGuard client
            536074627,  // SoftEther client
            551267005,  // Shadowsocks client
            608107152,  // SSH client
            718849192,  // OpenVPN obfs2/obfs3/scramble patch
            719849728,  // OpenVPN TAP emulator
            752704424,  // Auto connect
            777074589,  // Connect on demand
            823778945,  // Advanced split DNS
            946545622,  // Failover VPN profiles
            1122572344, // Tasker/Automate plug-in
            1172297389, // Port knocking
            1204386766, // On connect action
            1257343314, // Shortcuts
            1290565896, // Widgets
            1444998748, // Quick tiles
        )

        fun buildIdChecks(paramReg: String, labelPrefix: String, onMatch: String): String {
            val sb = StringBuilder()
            featureIds.forEachIndexed { idx, id ->
                val nextLabel = if (idx == featureIds.lastIndex) "${labelPrefix}_nomatch" else "${labelPrefix}_c${idx + 1}"
                sb.appendLine("    const v0, 0x${id.toUInt().toString(16)}")
                sb.appendLine("    if-eq $paramReg, v0, :${labelPrefix}_match")
                if (idx < featureIds.lastIndex) {
                    sb.appendLine("    :$nextLabel")
                }
            }
            sb.appendLine("    goto :${labelPrefix}_nomatch")
            sb.appendLine("    :${labelPrefix}_match")
            sb.appendLine("    $onMatch")
            sb.appendLine("    :${labelPrefix}_nomatch")
            return sb.toString()
        }

        // Central sync feature license gate: yj.n(int i) -> boolean. p1 = feature id.
        runCatching {
            FeatureLicensedFingerprint.method.addInstructions(
                0,
                buildIdChecks("p1", "vcp_n", "const/4 v0, 0x1\n    return v0")
            )
        }.onFailure {
            throw PatchException("Could not patch feature license check (yj.n).")
        }

        // Async feature authorization: yj.o(yj, int i, oc0) -> Object. p1 = feature id.
        val asyncSmali = buildIdChecks(
            "p1",
            "vcp_o",
            "sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;\n    return-object v0"
        )
        val asyncOk = runCatching {
            FeatureLicensedAsyncFingerprint.method.addInstructions(0, asyncSmali)
        }.isSuccess || runCatching {
            FeatureLicensedAsyncFallbackFingerprint.method.addInstructions(0, asyncSmali)
        }.isSuccess
        if (!asyncOk) {
            throw PatchException("Could not patch async feature check (yj.o).")
        }

        // Auto-connect UI gate (checked directly).
        runCatching {
            AutoConnectFeatureGateFingerprint.method.addInstructions(
                0,
                "const/4 v0, 0x1\n    return v0"
            )
        }
    }
}
