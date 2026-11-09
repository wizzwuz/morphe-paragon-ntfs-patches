package app.morphe.patches.mailnow.premium

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_MAILNOW = Compatibility(
        name = "Mail Now - Temp Mail",
        packageName = "air.kukulive.mailnow",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1A73E8,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true,
            ),
        ),
    )
}
