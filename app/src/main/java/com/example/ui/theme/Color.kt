package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Brand & Arcade Colors (inspired by the reference artwork)
val CosmicIndigoDark = Color(0xFF100E46)
val RoyalBlueDark = Color(0xFF1A237E)
val ElectricPurple = Color(0xFF6A1B9A)
val NeonMagenta = Color(0xFFE91E63)
val ArcadeGold = Color(0xFFFFD426)
val ArcadeOrange = Color(0xFFFF7A00)
val ArcadeRed = Color(0xFFE51937)
val BallBlue = Color(0xFF1EA8FF)
val BallPurple = Color(0xFF8B3DFF)
val BallOrange = Color(0xFFFF8C1A)
val BallGreen = Color(0xFF1CD760)
val BallPink = Color(0xFFFF3E9D)

// Light Mode Arcade Surface Colors
val SkyIndigoLight = Color(0xFFEEECFF)
val LavenderMistLight = Color(0xFFF6E8FF)
val PeachGlowLight = Color(0xFFFFEFE3)

@Immutable
data class GamePalette(
    val isDark: Boolean,
    val bgTop: Color,
    val bgMiddle: Color,
    val bgBottom: Color,
    val radialGlow: Color,
    val glassFill: Color,
    val glassFillElevated: Color,
    val glassBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val goldAccent: Color,
    val orangeAccent: Color,
    val pinkAccent: Color,
    val cyanAccent: Color,
    val greenAccent: Color,
    val dangerAccent: Color,
    val keypadSurface: Color,
    val keypadText: Color
)

val DarkGamePalette = GamePalette(
    isDark = true,
    bgTop = Color(0xFF12104A),
    bgMiddle = Color(0xFF2B146E),
    bgBottom = Color(0xFF4A1462),
    radialGlow = Color(0xFF283593),
    glassFill = Color(0x33FFFFFF),
    glassFillElevated = Color(0x47FFFFFF),
    glassBorder = Color(0x66FFFFFF),
    primaryText = Color(0xFFFFFFFF),
    secondaryText = Color(0xFFE2DBFF),
    goldAccent = ArcadeGold,
    orangeAccent = ArcadeOrange,
    pinkAccent = BallPink,
    cyanAccent = BallBlue,
    greenAccent = BallGreen,
    dangerAccent = Color(0xFFFF5252),
    keypadSurface = Color(0x3D171252),
    keypadText = Color(0xFFFFFFFF)
)

val LightGamePalette = GamePalette(
    isDark = false,
    bgTop = Color(0xFFE8E6FF),
    bgMiddle = Color(0xFFF5E1FF),
    bgBottom = Color(0xFFFFE6D6),
    radialGlow = Color(0xFFCBD4FF),
    glassFill = Color(0xB8FFFFFF),
    glassFillElevated = Color(0xDEFFFFFF),
    glassBorder = Color(0xFFFFFFFF),
    primaryText = Color(0xFF1B144A),
    secondaryText = Color(0xFF4D427A),
    goldAccent = Color(0xFFE5A400),
    orangeAccent = Color(0xFFE66000),
    pinkAccent = Color(0xFFD81B7A),
    cyanAccent = Color(0xFF0284C7),
    greenAccent = Color(0xFF159E47),
    dangerAccent = Color(0xFFD32F2F),
    keypadSurface = Color(0xCCFFFFFF),
    keypadText = Color(0xFF1B144A)
)

val LocalGamePalette = staticCompositionLocalOf { DarkGamePalette }
