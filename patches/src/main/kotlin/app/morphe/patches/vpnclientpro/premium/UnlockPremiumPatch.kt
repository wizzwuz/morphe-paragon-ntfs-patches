/*
 * VPN Client Pro (it.colucciweb.vpnclientpro) - unlock protocol IDs only.
 *
 * Analysis source: app-source/vpnclientpro_source (decompiled Java, m00, v1.02.57).
 *
 * Targets (feature/license gates only):
 * - Lyj;->n(I)Z : central feature-license check (used by ex0.b() requireFeature items).
 * - Lyj;->o(Lyj;I;Loc0;)Ljava/lang/Object; : async feature authorization (widgets/tiles/runtime).
 *
 * Unlocked feature IDs (from yj.j): protocol IDs only
 *   19339902 OpenVPN, 166729092 SSTP, 238425325 OpenConnect, 478614834 WireGuard,
 *   536074627 SoftEther, 551267005 Shadowsocks, 608107152 SSH.
 * All other feature IDs (obfs/scramble, TAP, auto-connect, connect-on-demand, split DNS,
 * failover, Tasker/Automate, port knocking, on-connect action, shortcuts, widgets, tiles)
 * are NOT forced unlocked to avoid triggering native VPN engine crashes.
 */
package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.vpnclientpro.premium.Constants

@Suppress("unused")
val unlockVpnClientProFeaturesPatch = bytecodePatch(
    name = "Unlock Protocols Only",
    description = "Unlocks only protocol selection in VPN Client Pro (it.colucciweb.vpnclientpro): OpenVPN, SSTP, OpenConnect, WireGuard, SoftEther, Shadowsocks, SSH. All pro features remain gated by original license checks to avoid native crashes.",
) {
    compatibleWith(Constants.COMPATIBILITY_VPNCLIENTPRO)

    execute {
        // Feature IDs (from yj.j, v1.02.57): PROTOCOL IDs ONLY.
        // All pro features (auto-connect, connect-on-demand, split DNS, failover,
        // Tasker/Automate, port knocking, on-connect action, shortcuts, widgets,
        // quick tiles, obfs/scramble, TAP) are intentionally NOT unlocked to avoid
        // triggering native OpenVPN library crashes (SIGSEGV in libopenvpn-2.5.so).
        val featureIds = listOf(
            19339902,   // OpenVPN client
            166729092,  // SSTP client
            238425325,  // OpenConnect client
            478614834,  // WireGuard client
            536074627,  // SoftEther client
            551267005,  // Shadowsocks client
            608107152,  // SSH client
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
    }
}
