package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val QubeeLightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimaryGreen,
    primaryContainer = MintGreen,
    onPrimaryContainer = OnMintGreen,
    secondary = TerracottaSecondary,
    onSecondary = OnTerracotta,
    secondaryContainer = SoftPink,
    onSecondaryContainer = OnSoftPink,
    tertiary = GoldAccent,
    onTertiary = OnGoldAccent,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = OnGoldContainer,
    background = CreamBackground,
    onBackground = DarkText,
    surface = WarmSurface,
    onSurface = DarkText,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val QubeeDarkColorScheme = darkColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = OnPrimaryGreenDark,
    primaryContainer = MintGreenDark,
    onPrimaryContainer = OnMintGreenDark,
    secondary = SecondaryPinkDark,
    onSecondary = OnSecondaryPinkDark,
    secondaryContainer = SoftPinkDark,
    onSecondaryContainer = OnSoftPinkDark,
    tertiary = GoldAccentDark,
    onTertiary = OnGoldAccentDark,
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = OnGoldContainerDark,
    background = NightBackground,
    onBackground = NightOnBackground,
    surface = NightSurface,
    onSurface = NightOnBackground,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

val QubeeShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun QubeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) QubeeDarkColorScheme else QubeeLightColorScheme
    val typography = scaledQubeeTypography(textScale.coerceIn(0.85f, 1.35f))

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = QubeeShapes,
        content = content
    )
}
