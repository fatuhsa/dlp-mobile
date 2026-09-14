package com.dlpmobile.core

import android.content.Context
import java.io.File

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

        // Compare asset size to detect upgrades without versioning the filename.
        val assetSize = context.assets.open(ASSET_PATH).use { it.available().toLong() }

        if (!destFile.exists() || destFile.length() != assetSize) {
            context.assets.open(ASSET_PATH).use { input ->
                destFile.outputStream().use { input.copyTo(it) }
            }
        }

        if (!destFile.canExecute()) destFile.setExecutable(true, true)

        return destFile
    }
}
