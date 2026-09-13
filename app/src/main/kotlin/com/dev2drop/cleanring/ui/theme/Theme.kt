package com.dev2drop.cleanring.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonEmerald,
    onPrimary = CyberBackground,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = NeonEmerald,
    secondary = ElectricBlue,
    onSecondary = CyberBackground,
    background = CyberBackground,
    onBackground = Color.White,
    surface = CyberSurface,
    onSurface = Color.White,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = Color(0xFF8C9BAE)
)

@Composable
fun SpamBlockerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
