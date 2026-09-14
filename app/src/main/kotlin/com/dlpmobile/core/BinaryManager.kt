package com.dlpmobile.core

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Manages the yt-dlp binary lifecycle:
 * - Extracts the bundled arm64-v8a binary from assets on first launch
 * - Re-extracts if the file is missing, empty, or lacks execute permission
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

        // Only re-extract when the file is missing, empty, or non-executable.
        // This avoids unnecessary re-extraction and correctly handles the
        // case where the file was copied without executable permission.
        if (!destFile.exists() || destFile.length() == 0L || !destFile.canExecute()) {
            context.assets.open(ASSET_PATH).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            // Ensure executable permission on all devices.
            destFile.setExecutable(true, false)
            destFile.setExecutable(true, true)
        }

        return destFile
    }
}
