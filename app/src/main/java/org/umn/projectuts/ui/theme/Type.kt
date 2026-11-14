package org.umn.projectuts.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SafeFontFamily: FontFamily = FontFamily.SansSerif

val SafeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 30.sp, lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp, lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.3.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SafeFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)
