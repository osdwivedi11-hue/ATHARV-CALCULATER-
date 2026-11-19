package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class LabExtendedColors(
    val displayBackground: Color,
    val displayGridDot: Color,
    val keyDigitContainer: Color,
    val keyDigitBorder: Color,
    val keyDigitContent: Color,
    val keyOperatorContainer: Color,
    val keyOperatorBorder: Color,
    val keyOperatorContent: Color,
    val keyScientificContainer: Color,
    val keyScientificBorder: Color,
    val keyScientificContent: Color,
    val keyDestructiveContainer: Color,
    val keyDestructiveBorder: Color,
    val keyDestructiveContent: Color,
    val keyEqualsContainer: Color,
    val keyEqualsContent: Color,
    val mutedText: Color,
    val elevatedCard: Color
)

private val DarkLabExtendedColors = LabExtendedColors(
    displayBackground = LabDarkDisplayBg,
    displayGridDot = LabDarkGridDot,
    keyDigitContainer = LabDarkKeyDigit,
    keyDigitBorder = LabDarkKeyDigitBorder,
    keyDigitContent = LabDarkOnBackground,
    keyOperatorContainer = LabDarkKeyOperator,
    keyOperatorBorder = LabDarkKeyOperatorBorder,
    keyOperatorContent = LabTealPrimary,
    keyScientificContainer = LabDarkKeyScientific,
    keyScientificBorder = LabDarkKeyScientificBorder,
    keyScientificContent = LabDarkKeyScientificText,
    keyDestructiveContainer = LabDarkKeyDestructive,
    keyDestructiveBorder = LabDarkKeyDestructiveBorder,
    keyDestructiveContent = LabDarkKeyDestructiveText,
    keyEqualsContainer = LabTealPrimary,
    keyEqualsContent = LabDarkOnPrimary,
    mutedText = LabDarkOnSurfaceMuted,
    elevatedCard = LabDarkSurfaceElevated
)

private val LightLabExtendedColors = LabExtendedColors(
    displayBackground = LabLightDisplayBg,
    displayGridDot = LabLightGridDot,
    keyDigitContainer = LabLightKeyDigit,
    keyDigitBorder = LabLightKeyDigitBorder,
    keyDigitContent = LabLightOnBackground,
    keyOperatorContainer = LabLightKeyOperator,
    keyOperatorBorder = LabLightKeyOperatorBorder,
    keyOperatorContent = LabTealDeep,
    keyScientificContainer = LabLightKeyScientific,
    keyScientificBorder = LabLightKeyScientificBorder,
    keyScientificContent = LabLightKeyScientificText,
    keyDestructiveContainer = LabLightKeyDestructive,
    keyDestructiveBorder = LabLightKeyDestructiveBorder,
    keyDestructiveContent = LabLightKeyDestructiveText,
    keyEqualsContainer = LabTealDeep,
    keyEqualsContent = LabLightOnPrimary,
    mutedText = LabLightOnSurfaceMuted,
    elevatedCard = LabLightSurfaceElevated
)

val LocalLabColors = staticCompositionLocalOf { DarkLabExtendedColors }

private val DarkLabColorScheme = darkColorScheme(
    primary = LabTealPrimary,
    onPrimary = LabDarkOnPrimary,
    primaryContainer = LabDarkKeyOperator,
    onPrimaryContainer = LabTealBright,
    secondary = LabDarkKeyScientificText,
    onSecondary = LabDarkOnPrimary,
    secondaryContainer = LabDarkKeyScientific,
    onSecondaryContainer = LabDarkKeyScientificText,
    tertiary = LabTealBright,
    onTertiary = LabDarkOnPrimary,
    background = LabDarkBackground,
    onBackground = LabDarkOnBackground,
    surface = LabDarkSurface,
    onSurface = LabDarkOnBackground,
    surfaceVariant = LabDarkSurfaceElevated,
    onSurfaceVariant = LabDarkOnSurfaceMuted,
    outline = LabDarkOutline,
    outlineVariant = LabDarkKeyDigitBorder,
    error = LabDarkKeyDestructiveText,
    onError = LabDarkOnPrimary,
    errorContainer = LabDarkKeyDestructive,
    onErrorContainer = LabDarkKeyDestructiveText
)

private val LightLabColorScheme = lightColorScheme(
    primary = LabTealDeep,
    onPrimary = LabLightOnPrimary,
    primaryContainer = LabLightKeyOperator,
    onPrimaryContainer = LabTealDeep,
    secondary = LabTealPrimary,
    onSecondary = LabLightOnPrimary,
    secondaryContainer = LabLightKeyScientific,
    onSecondaryContainer = LabLightKeyScientificText,
    tertiary = LabTealDeep,
    onTertiary = LabLightOnPrimary,
    background = LabLightBackground,
    onBackground = LabLightOnBackground,
    surface = LabLightSurface,
    onSurface = LabLightOnBackground,
    surfaceVariant = LabLightSurfaceElevated,
    onSurfaceVariant = LabLightOnSurfaceMuted,
    outline = LabLightOutline,
    outlineVariant = LabLightKeyDigitBorder,
    error = LabLightKeyDestructiveText,
    onError = LabLightOnPrimary,
    errorContainer = LabLightKeyDestructive,
    onErrorContainer = LabLightKeyDestructiveText
)

@Composable
fun LabCalculatorTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkLabColorScheme else LightLabColorScheme
    val extendedColors = if (darkTheme) DarkLabExtendedColors else LightLabExtendedColors

    CompositionLocalProvider(LocalLabColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LabTypography,
            content = content
        )
    }
}
