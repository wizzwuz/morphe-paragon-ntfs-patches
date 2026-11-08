package app.morphe.patches.flud.premium

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_FLUD = Compatibility(
        name = "Flud - Torrent Downloader",
        packageName = "com.delphicoder.flud",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xE53935,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true,
            ),
        ),
    )
}
