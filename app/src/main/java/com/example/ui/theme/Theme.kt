package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val VyroDarkColorScheme = darkColorScheme(
    primary = VyroAccentPrimary,
    onPrimary = Color.White,
    primaryContainer = VyroSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = VyroAccentSecondary,
    onSecondary = Color(0xFF080B14),
    tertiary = VyroAccentTertiary,
    background = VyroBackground,
    onBackground = VyroTextPrimary,
    surface = VyroSurface,
    onSurface = VyroTextPrimary,
    surfaceVariant = VyroSurfaceVariant,
    onSurfaceVariant = VyroTextSecondary,
    error = VyroError,
    onError = Color.White
)

private val VyroLightColorScheme = lightColorScheme(
    primary = VyroAccentPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E5FF),
    onPrimaryContainer = Color(0xFF1E1743),
    secondary = Color(0xFF129681),
    onSecondary = Color.White,
    tertiary = VyroAccentTertiary,
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF0E1322),
    surface = Color.White,
    onSurface = Color(0xFF0E1322),
    surfaceVariant = Color(0xFFECEFF5),
    onSurfaceVariant = Color(0xFF4B5565),
    error = VyroError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to premium dark 3D glassmorphic theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> VyroDarkColorScheme
        else -> VyroLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun VyroTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false, content = content)
