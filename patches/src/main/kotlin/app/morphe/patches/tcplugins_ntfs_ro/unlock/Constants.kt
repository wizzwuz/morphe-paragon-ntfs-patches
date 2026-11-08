package app.morphe.patches.tcplugins_ntfs_ro.unlock

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_NTFS_RO = Compatibility(
        name = "Total Commander NTFS Read-Only",
        packageName = "com.paragon.tcplugins_ntfs_ro",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x000000,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )
}
