package org.umn.projectuts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SafeLightColors = lightColorScheme(
    primary = UmnBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E5FB),
    onPrimaryContainer = Color(0xFF0A2547),
    secondary = SafeGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDF2D6),
    onSecondaryContainer = Color(0xFF07381A),
    tertiary = Color(0xFFB57A00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE9B0),
    onTertiaryContainer = Color(0xFF3B2800),
    error = SosRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410005),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF14233A),
    surface = Color.White,
    onSurface = Color(0xFF14233A),
    surfaceVariant = Color(0xFFE3EAF4),
    onSurfaceVariant = Color(0xFF52627A),
    outline = Color(0xFF8795AB),
    outlineVariant = Color(0xFFD2DBE8),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFD),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFEDF2F9),
    surfaceContainerHighest = Color(0xFFE3EAF4)
)

private val SafeDarkColors = darkColorScheme(
    primary = Color(0xFF8DB8F5),
    onPrimary = Color(0xFF06284F),
    primaryContainer = Color(0xFF1F4A85),
    onPrimaryContainer = Color(0xFFD7E5FB),
    secondary = SafeGreenSoft,
    onSecondary = Color(0xFF003915),
    secondaryContainer = Color(0xFF14602A),
    onSecondaryContainer = Color(0xFFCDF2D6),
    tertiary = Color(0xFFFFC857),
    onTertiary = Color(0xFF3B2800),
    tertiaryContainer = Color(0xFF5A3D00),
    onTertiaryContainer = Color(0xFFFFE9B0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B1B2E),
    onBackground = Color(0xFFE1E8F2),
    surface = Color(0xFF122840),
    onSurface = Color(0xFFE1E8F2),
    surfaceVariant = Color(0xFF1D3858),
    onSurfaceVariant = Color(0xFFB3C1D6),
    outline = Color(0xFF7F8FA6),
    outlineVariant = Color(0xFF2C4260),
    surfaceContainerLowest = Color(0xFF08162A),
    surfaceContainerLow = Color(0xFF0F2339),
    surfaceContainer = Color(0xFF122840),
    surfaceContainerHigh = Color(0xFF183250),
    surfaceContainerHighest = Color(0xFF1D3858)
)

@Composable
fun SafeUMNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) SafeDarkColors else SafeLightColors,
        typography = SafeTypography,
        shapes = SafeShapes,
        content = content
    )
}
