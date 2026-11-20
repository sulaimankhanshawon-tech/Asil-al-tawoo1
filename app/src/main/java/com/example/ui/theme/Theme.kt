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
    primary = SaudiEmeraldNeon,
    onPrimary = Color(0xFF00391F),
    primaryContainer = SaudiEmerald,
    onPrimaryContainer = SaudiEmeraldContainer,
    secondary = HeritageGoldNeon,
    onSecondary = Color(0xFF3B2A00),
    secondaryContainer = HeritageGoldDark,
    onSecondaryContainer = HeritageGoldContainer,
    tertiary = SaffronAmber,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = SaudiEmerald,
    onPrimary = Color.White,
    primaryContainer = SaudiEmeraldContainer,
    onPrimaryContainer = OnSaudiEmeraldContainer,
    secondary = HeritageGoldDark,
    onSecondary = Color.White,
    secondaryContainer = HeritageGoldContainer,
    onSecondaryContainer = OnHeritageGoldContainer,
    tertiary = SaffronAmber,
    background = WarmSandBg,
    onBackground = TextDarkPrimary,
    surface = CardSurfaceLight,
    onSurface = TextDarkPrimary,
    surfaceVariant = Color(0xFFF1EFEA),
    onSurfaceVariant = TextDarkSecondary,
    outline = BorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our custom Saudi culinary identity
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
