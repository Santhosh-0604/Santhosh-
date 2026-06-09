package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandRose,
    secondary = BrandMediumRose,
    tertiary = BrandRoseLight,
    background = Color(0xFF251E1D),
    surface = Color(0xFF332928),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BrandBackground,
    onSurface = BrandBackground,
    outline = BrandBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandRose,
    secondary = BrandMutedText,
    tertiary = BrandMediumRose,
    background = BrandBackground,
    surface = BrandWhiteAccent,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BrandDarkText,
    onSurface = BrandDarkText,
    surfaceVariant = BrandRoseMutedLight,
    onSurfaceVariant = BrandMutedText,
    outline = BrandBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to preserve the Geometric Balance brand palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
