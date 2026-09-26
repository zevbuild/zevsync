package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = MeshCyan80,
    onPrimary = Color(0xFF003731),
    primaryContainer = MeshCyan40,
    onPrimaryContainer = Color(0xFF73FBE4),
    secondary = MeshIndigo80,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = MeshIndigo30,
    onSecondaryContainer = Color(0xFFC7D2FE),
    tertiary = MeshCoral80,
    onTertiary = Color(0xFF4C0519),
    tertiaryContainer = MeshCoral40,
    onTertiaryContainer = Color(0xFFFFE4E6),
    background = MeshDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = MeshDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MeshDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    error = MeshDanger
)

private val LightColorScheme = lightColorScheme(
    primary = MeshCyan40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8FAF0),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = MeshIndigo40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = MeshCoral40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFF4C0519),
    background = MeshLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = MeshLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = MeshLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = MeshLightBorder,
    error = MeshDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark theme for peer mesh
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
