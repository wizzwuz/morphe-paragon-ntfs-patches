package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * VPN Client Pro (it.colucciweb.vpnclientpro) premium/license fingerprints.
 * Analysis based on decompiled source in app-source/vpnclientpro_source (m00).
 *
 * - ls: billing/license ViewModel (batch-3). m() returns owned license list (List<is>).
 *   n() queries billing and populates p; if billing not ready or no purchases, m() is empty.
 * - tn: auto-connect feature ViewModel (batch-5). f() gates auto-connect (error_disabled_feature).
 * - om4/yo4: UI screens that check ls.m().isEmpty() for license display.
 */

/** Returns the list of owned licenses (List<is>) from state p. */
internal object LicenseListFingerprint : Fingerprint(
    definingClass = "Lls;",
    name = "m",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

/** Auto-connect feature gate: returns o.getValue() boolean. */
internal object AutoConnectFeatureGateFingerprint : Fingerprint(
    definingClass = "Ltn;",
    name = "f",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

/** Billing/licenses UI: contains `if (lsVar.m().isEmpty())` gate (batch-4). */
internal object BillingUiLicenseEmptyFingerprint : Fingerprint(
    definingClass = "Lom4;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
)

/** Licenses/settings UI: contains `lsVar.m().isEmpty()` / size checks (batch-6). */
internal object LicensesUiFingerprint : Fingerprint(
    definingClass = "Lyo4;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
)
