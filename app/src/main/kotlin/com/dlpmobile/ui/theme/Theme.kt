package com.dlpmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand palette ─────────────────────────────────────────────────────────────

private val Teal = Color(0xFF00897B)
private val TealLight = Color(0xFF4DB6AC)
private val Blue = Color(0xFF1976D2)
private val SurfaceDark = Color(0xFF0D1B2A)
private val SurfaceDarkCard = Color(0xFF1B2838)
private val ErrorRed = Color(0xFFEF5350)

private val DarkColors = darkColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Blue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D47A1),
    onSecondaryContainer = Color(0xFFBBDEFB),
    background = SurfaceDark,
    surface = SurfaceDarkCard,
    surfaceVariant = Color(0xFF162232),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF90A4AE),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF37474F),
)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00332E),
    secondary = Blue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF0D47A1),
    background = Color(0xFFF5F7FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EDF2),
    onSurface = Color(0xFF1A2332),
    onSurfaceVariant = Color(0xFF546E7A),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFB0BEC5),
)

// ── Gradient colors (for Home screen) ─────────────────────────────────────

object GradientColors {
    val start = Color(0xFF00695C)
    val end = Color(0xFF1565C0)
}

// ── Theme composable ────────────────────────────────────────────────────

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
