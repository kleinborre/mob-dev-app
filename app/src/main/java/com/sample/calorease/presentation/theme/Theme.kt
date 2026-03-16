package com.sample.calorease.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CalorEaseLightColorScheme = lightColorScheme(
    primary             = LightPrimary,
    onPrimary           = LightOnPrimary,
    primaryContainer    = LightPrimaryContainer,
    onPrimaryContainer  = LightOnPrimaryContainer,
    secondary           = LightSecondary,
    onSecondary         = LightOnSecondary,
    background          = LightBackground,
    onBackground        = CharcoalGray,
    surface             = LightSurface,
    onSurface           = LightOnSurface,
    surfaceVariant      = LightSurfaceVariant,
    onSurfaceVariant    = LightOnSurfaceVariant,
    outline             = LightOutline,
    error               = LightError,
    onError             = LightOnError
)

private val CalorEaseDarkColorScheme = darkColorScheme(
    primary             = DarkPrimary,
    onPrimary           = DarkOnPrimary,
    primaryContainer    = DarkPrimaryContainer,
    onPrimaryContainer  = DarkOnPrimaryContainer,
    secondary           = DarkSecondary,
    onSecondary         = DarkOnSecondary,
    background          = DarkBackground,
    onBackground        = OnDarkSurface,
    surface             = DarkSurfaceColor,
    onSurface           = DarkOnSurfaceColor,
    surfaceVariant      = DarkSurfaceVariantColor,
    onSurfaceVariant    = DarkOnSurfaceVariant,
    outline             = DarkOutline,
    error               = DarkError,
    onError             = DarkOnError
)

@Composable
fun CalorEaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled — respect custom palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CalorEaseDarkColorScheme
        else      -> CalorEaseLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: transparent status bar, app draws behind it
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

