package com.example.e_inkoslauncher.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// =============================================================================
// Nothing OS Monochrome Typography
// Grotesk for UI, Dot-Matrix drawn manually in Canvas
// =============================================================================

// Inter for body/labels
val InterFamily = FontFamily.Default  // falls back to system sans-serif

val NothingTypography = Typography(
    // Hero: large display values (temps, battery %)
    displayLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize    = 42.sp,
        lineHeight  = 44.sp,
        letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize    = 32.sp,
        lineHeight  = 34.sp,
        letterSpacing = (-1.0).sp
    ),
    displaySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize    = 24.sp,
        lineHeight  = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    // Card title / event name
    titleLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize    = 16.sp,
        lineHeight  = 20.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 18.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize    = 12.sp,
        lineHeight  = 16.sp
    ),
    // Body text in cards
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize    = 14.sp,
        lineHeight  = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize    = 13.sp,
        lineHeight  = 18.sp
    ),
    // Captions, labels under icons
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize    = 11.sp,
        lineHeight  = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp
    )
)
