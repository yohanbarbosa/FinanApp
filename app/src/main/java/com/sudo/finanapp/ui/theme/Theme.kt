package com.sudo.finanapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

/** Tokens tomados 1:1 del diseño HTML (variables CSS). */
data class ThemeColors(
    val bg: Color, val surface: Color, val sv: Color, val bd: Color,
    val tx: Color, val tx2: Color, val brand: Color, val brandC: Color,
    val onBrand: Color, val inc: Color, val exp: Color, val warn: Color,
)

val LightColors = ThemeColors(
    bg = Color(0xFFF6F7FB), surface = Color(0xFFFFFFFF), sv = Color(0xFFEEF0F7), bd = Color(0xFFD5D9E6),
    tx = Color(0xFF10142B), tx2 = Color(0xFF4A5170), brand = Color(0xFF4338CA), brandC = Color(0xFFE0E7FF),
    onBrand = Color(0xFFFFFFFF), inc = Color(0xFF15803D), exp = Color(0xFFB91C1C), warn = Color(0xFFB45309),
)
val DarkColors = ThemeColors(
    bg = Color(0xFF0B1020), surface = Color(0xFF141A31), sv = Color(0xFF1D2542), bd = Color(0xFF2D3658),
    tx = Color(0xFFEEF0FA), tx2 = Color(0xFFA3AAC8), brand = Color(0xFFA5B4FC), brandC = Color(0xFF2A2F73),
    onBrand = Color(0xFF1E1B4B), inc = Color(0xFF4ADE80), exp = Color(0xFFF87171), warn = Color(0xFFFBBF24),
)

val LocalCuadre = staticCompositionLocalOf { LightColors }

object Cuadre {
    val colors: ThemeColors @Composable get() = LocalCuadre.current
}

@Composable
fun CuadreTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val c = if (dark) DarkColors else LightColors
    val scheme = (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = c.brand, onPrimary = c.onBrand, background = c.bg, onBackground = c.tx,
        surface = c.surface, onSurface = c.tx, surfaceVariant = c.sv, outline = c.bd,
        primaryContainer = c.brandC, onPrimaryContainer = c.tx, error = c.exp,
        secondaryContainer = c.brandC, onSecondaryContainer = c.tx,
    )
    CompositionLocalProvider(LocalCuadre provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
