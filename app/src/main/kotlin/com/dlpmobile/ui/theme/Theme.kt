package com.dlpmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand palette ─────────────────────────────────────────────────────────────

private val Red = Color(0xFFE53935)
private val RedDark = Color(0xFFC62828)
private val Surface = Color(0xFF121212)

private val DarkColors = darkColorScheme(
    primary = Red,
    onPrimary = Color.White,
    secondary = Color(0xFFFF6D6D),
    background = Surface,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
)

private val LightColors = lightColorScheme(
    primary = Red,
    onPrimary = Color.White,
    secondary = RedDark,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
)

// ── Theme composable ──────────────────────────────────────────────────────────

@Composable
fun DlpTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
