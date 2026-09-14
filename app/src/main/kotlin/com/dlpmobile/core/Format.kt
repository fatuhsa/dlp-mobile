package com.dlpmobile.core

/** A single downloadable format entry from yt-dlp --list-formats. */
data class Format(
    /** Format ID passed to yt-dlp -f */
    val id: String,
    /** Human-readable extension, e.g. "mp4", "webm" */
    val ext: String,
    /** Resolution string, e.g. "1080p", "audio only" */
    val resolution: String,
    /** Codec info, e.g. "avc1+mp4a" */
    val codec: String,
    /** Approximate file size (may be null if yt-dlp doesn't report it) */
    val fileSizeApprox: String?,
    /** Whether this format contains video */
    val hasVideo: Boolean,
    /** Whether this format contains audio */
    val hasAudio: Boolean,
)
