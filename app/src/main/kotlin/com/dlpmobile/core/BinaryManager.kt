package com.dlpmobile.core

import android.content.Context
import android.os.Build
import java.io.File

/**
 * Manages the yt-dlp binary lifecycle:
 * - Selects the correct ABI binary from assets
 * - Extracts it to the app's internal files directory on first launch
 * - Ensures it has executable permissions
 *
 * Asset layout expected:
 *   assets/bin/arm64-v8a/yt-dlp
 *   assets/bin/armeabi-v7a/yt-dlp
 *   assets/bin/x86_64/yt-dlp
 */
object BinaryManager {

    private const val BINARY_NAME = "yt-dlp"
    private const val ASSET_DIR = "bin"

    /** Returns the [File] pointing to the ready-to-run yt-dlp binary. */
    fun getOrExtract(context: Context): File {
        val destFile = File(context.filesDir, BINARY_NAME)

        // Always re-extract on version upgrades; a simple approach is to
        // compare the asset size with the extracted file size.
        val assetPath = "$ASSET_DIR/${pickAbi()}/$BINARY_NAME"
        val assetSize = context.assets.open(assetPath).use { it.available().toLong() }

        if (!destFile.exists() || destFile.length() != assetSize) {
            extract(context, assetPath, destFile)
        }

        if (!destFile.canExecute()) {
            destFile.setExecutable(true, true)
        }

        return destFile
    }

    private fun extract(context: Context, assetPath: String, dest: File) {
        context.assets.open(assetPath).use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        dest.setExecutable(true, true)
    }

    /**
     * Picks the best ABI from the supported ABIs reported by the device.
     * Falls back to armeabi-v7a if nothing better is found.
     */
    private fun pickAbi(): String {
        val supported = Build.SUPPORTED_ABIS.toList()
        return when {
            supported.contains("arm64-v8a")   -> "arm64-v8a"
            supported.contains("x86_64")      -> "x86_64"
            supported.contains("armeabi-v7a") -> "armeabi-v7a"
            else -> "armeabi-v7a"
        }
    }
}
