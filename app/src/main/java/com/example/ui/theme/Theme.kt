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

private val NexaDarkColorScheme = darkColorScheme(
    primary = NexaVioletPrimary,
    onPrimary = Color.White,
    primaryContainer = NexaVioletDark,
    onPrimaryContainer = NexaVioletLight,
    secondary = NexaCyanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = NexaCyanDark,
    onSecondaryContainer = NexaCyanLight,
    tertiary = NexaAccentPink,
    onTertiary = Color.White,
    background = NexaDarkBackground,
    onBackground = NexaDarkOnBackground,
    surface = NexaDarkSurface,
    onSurface = NexaDarkOnSurface,
    surfaceVariant = NexaDarkSurfaceVariant,
    onSurfaceVariant = NexaDarkOnSurfaceVariant,
    outline = NexaDarkBorder,
    outlineVariant = Color(0xFF1E293B)
)

private val NexaLightColorScheme = lightColorScheme(
    primary = NexaVioletDark,
    onPrimary = Color.White,
    primaryContainer = NexaVioletLight,
    onPrimaryContainer = NexaVioletDark,
    secondary = NexaCyanDark,
    onSecondary = Color.White,
    secondaryContainer = NexaCyanLight,
    onSecondaryContainer = NexaCyanDark,
    tertiary = NexaAccentPink,
    onTertiary = Color.White,
    background = NexaLightBackground,
    onBackground = NexaLightOnBackground,
    surface = NexaLightSurface,
    onSurface = NexaLightOnSurface,
    surfaceVariant = NexaLightSurfaceVariant,
    onSurfaceVariant = NexaLightOnSurfaceVariant,
    outline = NexaLightBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to Nexa AI signature styling
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NexaDarkColorScheme
        else -> NexaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
