package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ArmyXColorScheme = darkColorScheme(
    primary = TacticalOrange,
    onPrimary = GunmetalDark,
    primaryContainer = TacticalOrangeBright,
    onPrimaryContainer = GunmetalDark,
    secondary = MilitaryOliveLight,
    onSecondary = GunmetalDark,
    secondaryContainer = MilitaryOlive,
    onSecondaryContainer = TextWhite,
    tertiary = AccentCyan,
    onTertiary = GunmetalDark,
    background = GunmetalDark,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = TextMuted,
    outline = SurfaceBorder,
    error = DangerRed,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tactical military identity consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArmyXColorScheme,
        typography = Typography,
        content = content
    )
}
