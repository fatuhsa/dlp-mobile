package com.dlpmobile.core

import android.content.Context
import android.content.res.AssetFileDescriptor
import java.io.File
import java.io.FileOutputStream

/**
 * Manages the yt-dlp binary lifecycle:
 * - Extracts the bundled arm64-v8a binary from assets on first launch
 * - Re-extracts if the file is missing or the size changed (version upgrade)
 * - Ensures the file has executable permissions
 *
 * Asset layout:
 *   assets/bin/arm64-v8a/yt-dlp
 */
object BinaryManager {

    private const val BINARY_NAME = "yt-dlp"
    private const val ASSET_PATH  = "bin/arm64-v8a/yt-dlp"

    /** Returns the [File] pointing to the ready-to-run yt-dlp binary. */
    fun getOrExtract(context: Context): File {
        val destFile = File(context.filesDir, BINARY_NAME)

        val assetSize = getAssetSize(context)

        if (!destFile.exists() || destFile.length() != assetSize) {
            context.assets.open(ASSET_PATH).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        // Ensure executable permission — required on some devices where
        // copied files lose the execute bit.
        if (!destFile.canExecute()) {
            destFile.setExecutable(true, false)
        }
        if (!destFile.canExecute()) {
            destFile.setExecutable(true, true)
        }

        return destFile
    }

    private fun getAssetSize(context: Context): Long {
        val fd: AssetFileDescriptor = context.assets.openFd(ASSET_PATH)
        return try {
            fd.length
        } finally {
            fd.close()
        }
    }
}
