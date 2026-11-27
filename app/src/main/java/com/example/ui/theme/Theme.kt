package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val DarkColorScheme = darkColorScheme(
    primary = ArcadeGold,
    onPrimary = Color(0xFF1F1200),
    secondary = BallPink,
    onSecondary = Color.White,
    tertiary = BallBlue,
    onTertiary = Color.White,
    background = CosmicIndigoDark,
    onBackground = Color.White,
    surface = Color(0xFF211761),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2F217D),
    onSurfaceVariant = Color(0xFFDAD3FF),
    error = Color(0xFFFF5252),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6A1B9A),
    onPrimary = Color.White,
    secondary = Color(0xFFD81B7A),
    onSecondary = Color.White,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = SkyIndigoLight,
    onBackground = Color(0xFF1B144A),
    surface = Color.White,
    onSurface = Color(0xFF1B144A),
    surfaceVariant = Color(0xFFEDE7FF),
    onSurfaceVariant = Color(0xFF4D427A),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun GuessTheNumberTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val gamePalette = if (darkTheme) DarkGamePalette else LightGamePalette

    CompositionLocalProvider(LocalGamePalette provides gamePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
