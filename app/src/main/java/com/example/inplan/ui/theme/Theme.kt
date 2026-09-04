package com.example.inplan.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InPlanColorScheme = lightColorScheme(

    // Brand
    primary = InPlanBlue,
    onPrimary = InPlanWhite,

    secondary = InPlanBlueLight,
    onSecondary = InPlanWhite,

    tertiary = OwesOrange,
    onTertiary = InPlanWhite,

    // Backgrounds
    background = BgBase,
    onBackground = TextPrimary,

    surface = CardSurface,
    onSurface = TextPrimary,

    surfaceVariant = BgScaffold,
    onSurfaceVariant = TextSecondary,

    // States
    error = ErrorRed,
    onError = InPlanWhite,

    outline = CardBorder
)

private val InPlanShapes = Shapes(

    extraSmall = RoundedCornerShape(8.dp),

    small = RoundedCornerShape(12.dp),

    medium = RoundedCornerShape(18.dp),

    large = RoundedCornerShape(24.dp),

    extraLarge = RoundedCornerShape(32.dp)
)

private val InPlanTypography = Typography(

    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        letterSpacing = (-0.5).sp
    ),

    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = (-0.3).sp
    ),

    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),

    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp
    ),

    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),

    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    ),

    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)

@Composable
fun InPlanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = InPlanColorScheme,
        typography = InPlanTypography,
        shapes = InPlanShapes,
        content = content
    )
}