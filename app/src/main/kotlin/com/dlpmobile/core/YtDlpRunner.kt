package com.dlpmobile.core

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Thin wrapper around yt-dlp subprocess execution.
 *
 * All public functions return a [Flow] of log lines so the UI can show live
 * progress without blocking the main thread.
 */
class YtDlpRunner(private val context: Context) {

    private val binary: File by lazy { BinaryManager.getOrExtract(context) }

    // ── Format listing ────────────────────────────────────────────────────────

    /**
     * Fetches available formats for [url].
     * Emits raw yt-dlp output lines; call [parseFormats] to convert them.
     */
    fun listFormats(url: String): Flow<String> = runCommand(
        args = listOf("--list-formats", "--no-playlist", url)
    )

    /**
     * Parses the raw `--list-formats` output into [Format] objects.
     * Call this after collecting all lines from [listFormats].
     */
    fun parseFormats(lines: List<String>): List<Format> {
        // yt-dlp format lines look like:
        // ID   EXT   RESOLUTION  FPS  CH  FILESIZE  TBR  PROTO  VCODEC  VBR  ACODEC  ABR  ASR  MORE INFO
        // 137  mp4   1920x1080    25       ~123.4MiB  ...  avc1   ...   mp4a ...
        val headerIdx = lines.indexOfFirst { it.trimStart().startsWith("ID") }
        if (headerIdx < 0) return emptyList()

        return lines.drop(headerIdx + 1)
            .filter { it.isNotBlank() && !it.startsWith("-") }
            .mapNotNull { line -> parseSingleFormat(line) }
    }

    private fun parseSingleFormat(line: String): Format? {
        // Split on 2+ spaces to handle variable-width columns.
        val cols = line.trim().split(Regex("\\s{2,}"))
        if (cols.size < 3) return null

        val id = cols[0]
        val ext = cols[1]
        val resolution = cols[2]

        // Detect audio-only rows (yt-dlp uses "audio only" in the resolution col)
        val isAudioOnly = resolution.contains("audio only", ignoreCase = true)
        val hasVideo = !isAudioOnly
        val hasAudio = isAudioOnly || cols.getOrNull(7)?.contains("audio", ignoreCase = true) == true

        // Look for a filesize column that contains a size pattern
        val sizePattern = Regex("""~?\d+(\.\d+)?\s*(KiB|MiB|GiB|KB|MB|GB)""", RegexOption.IGNORE_CASE)
        val fileSizeApprox = cols.firstOrNull { sizePattern.containsMatchIn(it) }

        val codec = buildString {
            val vcodec = cols.getOrNull(8)?.takeIf { it != "unknown" && it != "none" }
            val acodec = cols.getOrNull(10)?.takeIf { it != "unknown" && it != "none" }
            if (vcodec != null) append(vcodec)
            if (vcodec != null && acodec != null) append("+")
            if (acodec != null) append(acodec)
        }

        return Format(
            id = id,
            ext = ext,
            resolution = resolution,
            codec = codec,
            fileSizeApprox = fileSizeApprox,
            hasVideo = hasVideo,
            hasAudio = hasAudio,
        )
    }

    // ── Downloading ───────────────────────────────────────────────────────────

    /**
     * Downloads [url] in format [formatId] to the public Downloads directory.
     * Emits yt-dlp stdout/stderr lines (including the [download] progress lines).
     */
    fun download(url: String, formatId: String): Flow<String> {
        val outputDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .absolutePath

        return runCommand(
            args = listOf(
                "--format", formatId,
                "--merge-output-format", "mp4",
                "--output", "$outputDir/%(title)s.%(ext)s",
                "--no-playlist",
                "--newline",   // one progress line per line (easier to parse)
                url,
            )
        )
    }

    // ── Low-level execution ───────────────────────────────────────────────────

    /**
     * Runs the yt-dlp binary with [args] and emits each output line as it
     * arrives.  Both stdout and stderr are merged.
     */
    private fun runCommand(args: List<String>): Flow<String> = flow {
        val cmd = mutableListOf(binary.absolutePath) + args
        val process = ProcessBuilder(cmd)
            .redirectErrorStream(true)   // merge stderr into stdout
            .directory(context.filesDir) // working dir = private files dir
            .start()

        process.inputStream.bufferedReader().use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                emit(line!!)
            }
        }

        val exitCode = withContext(Dispatchers.IO) { process.waitFor() }
        if (exitCode != 0) {
            emit("[ERROR] yt-dlp exited with code $exitCode")
        }
    }.flowOn(Dispatchers.IO)
}
