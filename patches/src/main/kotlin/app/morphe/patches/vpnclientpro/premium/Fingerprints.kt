package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * VPN Client Pro (it.colucciweb.vpnclientpro) feature/license fingerprints.
 * Analysis based on decompiled source in app-source/vpnclientpro_source (m00), v1.02.57.
 *
 * - yj.n(int): central licensed-feature check (returns true if feature ID is in licensed set).
 *   Protocol IDs: 19339902 OpenVPN, 166729092 SSTP, 238425325 OpenConnect, 478614834 WireGuard,
 *   536074627 SoftEther, 551267005 Shadowsocks, 608107152 SSH.
 *   Other feature IDs (obfs/scramble, TAP, auto-connect, connect-on-demand, split DNS, failover,
 *   Tasker/Automate, port knocking, on-connect action, shortcuts, widgets, tiles) are NOT
 *   forced unlocked to avoid triggering native OpenVPN crashes.
 * - ex0.b(): feature menu item blocked when requireFeature!=0 && !yj.n(id).
 */

/** Central feature license check: returns true if feature id is licensed. */
internal object FeatureLicensedFingerprint : Fingerprint(
    definingClass = "Lyj;",
    name = "n",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("I"),
)

/** Async/suspend feature authorization (e.g. widgets): returns Boolean.TRUE when licensed. */
internal object FeatureLicensedAsyncFingerprint : Fingerprint(
    definingClass = "Lyj;",
    name = "o",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lyj;", "I", "Loc0;"),
)

/** Fallback: async feature authorization with broader parameter matching. */
internal object FeatureLicensedAsyncFallbackFingerprint : Fingerprint(
    definingClass = "Lyj;",
    name = "o",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/Object;",
)
