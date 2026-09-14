package com.dlpmobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dlpmobile.core.Format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatPickerScreen(
    formats: List<Format>,
    selectedFormat: Format?,
    onBack: () -> Unit,
    onSelect: (Format) -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick a format") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onDownload,
                    enabled = selectedFormat != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                ) {
                    Text("Download${selectedFormat?.let { " (${it.ext} · ${it.resolution})" } ?: ""}")
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
        ) {
            val videoAudio = formats.filter { it.hasVideo && it.hasAudio }
            val videoOnly  = formats.filter { it.hasVideo && !it.hasAudio }
            val audioOnly  = formats.filter { !it.hasVideo && it.hasAudio }

            if (videoAudio.isNotEmpty()) {
                item { SectionHeader("Video + Audio") }
                items(videoAudio) { fmt ->
                    FormatRow(fmt, isSelected = fmt == selectedFormat, onSelect = onSelect)
                }
            }
            if (videoOnly.isNotEmpty()) {
                item { SectionHeader("Video only") }
                items(videoOnly) { fmt ->
                    FormatRow(fmt, isSelected = fmt == selectedFormat, onSelect = onSelect)
                }
            }
            if (audioOnly.isNotEmpty()) {
                item { SectionHeader("Audio only") }
                items(audioOnly) { fmt ->
                    FormatRow(fmt, isSelected = fmt == selectedFormat, onSelect = onSelect)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun FormatRow(
    format: Format,
    isSelected: Boolean,
    onSelect: (Format) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onSelect(format) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${format.resolution}  ·  ${format.ext.uppercase()}",
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val extra = buildString {
                    if (format.codec.isNotBlank()) append(format.codec)
                    format.fileSizeApprox?.let { append("  ·  ~$it") }
                }
                if (extra.isNotBlank()) {
                    Text(
                        text = extra,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}
