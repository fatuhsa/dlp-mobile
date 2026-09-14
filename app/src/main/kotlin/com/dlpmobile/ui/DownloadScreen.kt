package com.dlpmobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Download screen: shows a live log of yt-dlp output with a progress
 * spinner while in progress, a success state, or an error state.
 *
 * @param isDownloading   Whether download is still running
 * @param logLines        Live stdout lines from yt-dlp
 * @param error           Error message, or null
 * @param isFinished      True when download completed successfully
 * @param onCancel        Cancel the in-progress download
 * @param onDone          Navigate back to Home after success/error
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    isDownloading: Boolean,
    logLines: List<String>,
    error: String?,
    isFinished: Boolean,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom as new lines arrive
    LaunchedEffect(logLines.size) {
        if (logLines.isNotEmpty()) {
            listState.animateScrollToItem(logLines.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            when {
                                isFinished    -> "Download complete ✓"
                                error != null -> "Download failed"
                                else          -> "Downloading…"
                            }
                        )
                    }
                },
                navigationIcon = {
                    if (!isDownloading) {
                        IconButton(onClick = onDone) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isDownloading) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!isDownloading) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFinished)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(if (isFinished) "Done" else "Back")
                    }
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            // Live log output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
            ) {
                items(logLines) { line ->
                    // Highlight progress lines that start with [download]
                    val isProgress = line.trimStart().startsWith("[download]")
                    Text(
                        text = line,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = when {
                            isProgress -> MaterialTheme.colorScheme.primary
                            line.contains("[ERROR]", ignoreCase = true) ->
                                MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        },
                        modifier = Modifier.padding(vertical = 1.dp),
                    )
                }
            }
        }
    }
}
