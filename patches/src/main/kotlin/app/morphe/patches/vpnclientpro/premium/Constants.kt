package app.morphe.patches.vpnclientpro.premium

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_VPNCLIENTPRO = Compatibility(
        name = "VPN Client Pro",
        packageName = "it.colucciweb.vpnclientpro",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2E7D32,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true,
            ),
        ),
    )
}
