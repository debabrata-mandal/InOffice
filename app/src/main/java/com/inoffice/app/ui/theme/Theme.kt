package com.inoffice.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Blues aligned with [RummyPulse](https://github.com/debabrata-mandal/RummyPulse) (Material blue + dark slate surfaces).
 */
private val RummyPulseLight =
    lightColorScheme(
        primary = Color(0xFF1976D2),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF2196F3),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE3F2FD),
        onSecondaryContainer = Color(0xFF0D47A1),
        tertiary = Color(0xFF0288D1),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFB3E5FC),
        onTertiaryContainer = Color(0xFF01579B),
        background = Color(0xFFF0F7FD),
        onBackground = Color(0xFF1E3A5F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1E3A5F),
        surfaceVariant = Color(0xFFE3ECF5),
        onSurfaceVariant = Color(0xFF3E5C85),
        outline = Color(0xFF90CAF9),
        outlineVariant = Color(0xFFB0BEC5),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFE8F2FA),
        surfaceContainer = Color(0xFFDFEDF8),
        surfaceContainerHigh = Color(0xFFD2E6F5),
        surfaceContainerHighest = Color(0xFFC4DAEF),
    )

private val RummyPulseDark =
    darkColorScheme(
        primary = Color(0xFF64B5F6),
        onPrimary = Color(0xFF0D47A1),
        primaryContainer = Color(0xFF20324C),
        onPrimaryContainer = Color(0xFFBBDEFB),
        secondary = Color(0xFF2196F3),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF3D5C85),
        onSecondaryContainer = Color(0xFFE8F4FF),
        tertiary = Color(0xFF4FC3F7),
        onTertiary = Color(0xFF00363D),
        tertiaryContainer = Color(0xFF2A4A62),
        onTertiaryContainer = Color(0xFFE1F5FE),
        background = Color(0xFF121C2E),
        onBackground = Color(0xFFE4EDF8),
        surface = Color(0xFF1A2940),
        onSurface = Color(0xFFE4EDF8),
        surfaceVariant = Color(0xFF243652),
        onSurfaceVariant = Color(0xFFB8C9E0),
        outline = Color(0xFF4A6FA0),
        outlineVariant = Color(0xFF324A68),
        surfaceContainerLowest = Color(0xFF0E1624),
        surfaceContainerLow = Color(0xFF162536),
        surfaceContainer = Color(0xFF1C3048),
        surfaceContainerHigh = Color(0xFF28456A),
        surfaceContainerHighest = Color(0xFF345A88),
    )

@Composable
fun InOfficeTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colorScheme = if (dark) RummyPulseDark else RummyPulseLight
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
