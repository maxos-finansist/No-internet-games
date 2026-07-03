package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    secondary = SophisticatedSecondary,
    onSecondary = SophisticatedOnSecondary,
    tertiary = SophisticatedAccent,
    background = SophisticatedBackground,
    onBackground = SophisticatedText,
    surface = SophisticatedSurface,
    onSurface = SophisticatedText,
    surfaceVariant = SophisticatedSurface,
    onSurfaceVariant = SophisticatedSubtext,
    outline = SophisticatedOutline
  )

private val LightColorScheme = DarkColorScheme // Keep same arcade theme in light mode for retro consistency

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
