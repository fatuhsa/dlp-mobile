package com.dlpmobile.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dlpmobile.core.Format
import com.dlpmobile.core.YtDlpRunner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UI state ──────────────────────────────────────────────────────────────────

data class MainUiState(
    // Home screen
    val url: String = "",

    // Format fetch
    val isFetchingFormats: Boolean = false,
    val formats: List<Format> = emptyList(),
    val fetchError: String? = null,

    // Download
    val selectedFormat: Format? = null,
    val isDownloading: Boolean = false,
    val downloadLog: List<String> = emptyList(),
    val downloadError: String? = null,
    val downloadFinished: Boolean = false,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val runner = YtDlpRunner(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null
    private var downloadJob: Job? = null

    // ── URL input ─────────────────────────────────────────────────────────────

    fun onUrlChange(newUrl: String) {
        _uiState.update { it.copy(url = newUrl, fetchError = null) }
    }

    /** Called when the user pastes a URL from the share sheet. */
    fun onSharedUrl(url: String) {
        _uiState.update { it.copy(url = url) }
    }

    // ── Format fetching ───────────────────────────────────────────────────────

    fun fetchFormats() {
        val url = _uiState.value.url.trim()
        if (url.isBlank()) return

        fetchJob?.cancel()
        _uiState.update {
            it.copy(
                isFetchingFormats = true,
                formats = emptyList(),
                fetchError = null,
                selectedFormat = null,
            )
        }

        fetchJob = viewModelScope.launch {
            val lines = mutableListOf<String>()
            try {
                runner.listFormats(url).collect { line -> lines += line }
                val formats = runner.parseFormats(lines)
                if (formats.isEmpty()) {
                    _uiState.update {
                        it.copy(isFetchingFormats = false, fetchError = "No formats found.")
                    }
                } else {
                    _uiState.update {
                        it.copy(isFetchingFormats = false, formats = formats)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isFetchingFormats = false, fetchError = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun onFormatSelected(format: Format) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    // ── Downloading ───────────────────────────────────────────────────────────

    fun startDownload() {
        val url = _uiState.value.url.trim()
        val format = _uiState.value.selectedFormat ?: return

        downloadJob?.cancel()
        _uiState.update {
            it.copy(
                isDownloading = true,
                downloadLog = emptyList(),
                downloadError = null,
                downloadFinished = false,
            )
        }

        downloadJob = viewModelScope.launch {
            try {
                runner.download(url, format.id).collect { line ->
                    _uiState.update { it.copy(downloadLog = it.downloadLog + line) }
                }
                _uiState.update { it.copy(isDownloading = false, downloadFinished = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isDownloading = false, downloadError = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _uiState.update { it.copy(isDownloading = false) }
    }

    fun resetDownload() {
        _uiState.update {
            it.copy(
                selectedFormat = null,
                isDownloading = false,
                downloadLog = emptyList(),
                downloadError = null,
                downloadFinished = false,
            )
        }
    }
}
