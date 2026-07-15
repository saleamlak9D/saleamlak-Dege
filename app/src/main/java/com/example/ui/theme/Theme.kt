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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF193A00),
    primaryContainer = Color(0xFF285300),
    onPrimaryContainer = Color(0xFFB8F397),
    secondary = SecondaryDark,
    onSecondary = Color(0xFF223514),
    secondaryContainer = Color(0xFF334C20),
    onSecondaryContainer = Color(0xFFDCE8CE),
    tertiary = TertiaryDark,
    onTertiary = Color(0xFF1D3528),
    background = BackgroundDark,
    onBackground = Color(0xFFE1E4DA),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E4DA),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC1C9B7),
    outline = Color(0xFF8D9286),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE6D3),
    onPrimaryContainer = Color(0xFF191C17),
    secondary = SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0F5E9),
    onSecondaryContainer = Color(0xFF191C17),
    tertiary = TertiaryLight,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF191C17),
    surface = SurfaceLight,
    onSurface = Color(0xFF191C17),
    surfaceVariant = Color(0xFFF0F5E9),
    onSurfaceVariant = Color(0xFF43483E),
    outline = Color(0xFFC1C9B7),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun SalishELearningTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set false to preserve our custom Salish branded colors strictly!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
