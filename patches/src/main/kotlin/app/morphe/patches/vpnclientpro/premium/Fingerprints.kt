package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * VPN Client Pro (it.colucciweb.vpnclientpro) feature/license fingerprints.
 * Analysis based on decompiled source in app-source/vpnclientpro_source (m00), v1.02.57.
 *
 * - yj.n(int): central licensed-feature check (returns true if feature ID is in licensed set).
 *   Feature IDs: 718849192 obfs/scramble, 719849728 TAP emulator, 752704424 auto-connect,
 *   777074589 connect on demand, 823778945 advanced split DNS, 946545622 failover,
 *   1122572344 Tasker/Automate, 1172297389 port knocking, 1204386766 on-connect action,
 *   1257343314 shortcuts, 1290565896 widgets, 1444998748 quick tiles, etc.
 * - ex0.b(): feature menu item blocked when requireFeature!=0 && !yj.n(id).
 * - tn.f(): auto-connect ViewModel gate (checked directly in UI).
 */

/** Central feature license check: returns true if feature id is licensed. */
internal object FeatureLicensedFingerprint : Fingerprint(
    definingClass = "Lyj;",
    name = "n",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("I"),
)

/** Auto-connect feature gate: returns o.getValue() boolean. */
internal object AutoConnectFeatureGateFingerprint : Fingerprint(
    definingClass = "Ltn;",
    name = "f",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)
