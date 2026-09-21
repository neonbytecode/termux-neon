package dev.neonbytecode.neon.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ------------------------------------------------------------------ */
/*  NEON tokens — premium dark base, neon accents, disciplined glow.   */
/* ------------------------------------------------------------------ */

val NeonCyan = Color(0xFF00E5FF)
val NeonMagenta = Color(0xFFFF00E5)
val NeonViolet = Color(0xFF7C5CFF)
val NeonGreen = Color(0xFF00FF9F)
val NeonRed = Color(0xFFFF2D55)
val NeonAmber = Color(0xFFFFB000)

val NeonBg = Color(0xFF0A0A12)
val NeonBgAlt = Color(0xFF0E0E1A)
val NeonSurface = Color(0xFF12121E)
val NeonSurfaceHigh = Color(0xFF1A1A2A)
val NeonOutline = Color(0xFF2E2E40)
val NeonTextPrimary = Color(0xFFE8E8F0)
val NeonTextSecondary = Color(0xFF9393AA)

private val NeonDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001A1F),
    primaryContainer = Color(0xFF003A42),
    onPrimaryContainer = Color(0xFF00E5FF),
    secondary = NeonMagenta,
    onSecondary = Color(0xFF3D0033),
    secondaryContainer = Color(0xFF4D003F),
    onSecondaryContainer = Color(0xFFFF80EB),
    tertiary = NeonViolet,
    onTertiary = Color(0xFF1A1038),
    background = NeonBg,
    onBackground = NeonTextPrimary,
    surface = NeonSurface,
    onSurface = NeonTextPrimary,
    surfaceVariant = NeonSurfaceHigh,
    onSurfaceVariant = NeonTextSecondary,
    surfaceContainerLowest = Color(0xFF08080F),
    surfaceContainerLow = Color(0xFF10101A),
    surfaceContainer = Color(0xFF12121E),
    surfaceContainerHigh = Color(0xFF181828),
    surfaceContainerHighest = Color(0xFF1E1E30),
    outline = NeonOutline,
    outlineVariant = Color(0xFF24243A),
    error = NeonRed,
    onError = Color(0xFF2E0008),
    errorContainer = Color(0xFF4D0010),
    onErrorContainer = Color(0xFFFFB0BD),
    inverseSurface = Color(0xFFE8E8F0),
    inverseOnSurface = Color(0xFF14141F),
)

private val NeonDayColorScheme = lightColorScheme(
    primary = Color(0xFF006F7A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB4ECF5),
    onPrimaryContainer = Color(0xFF002F34),
    secondary = Color(0xFF8E1168),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF17171E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17171E),
    surfaceVariant = Color(0xFFE9EAF1),
    onSurfaceVariant = Color(0xFF45464E),
    outline = Color(0xFF75767F),
    error = Color(0xFFBA1A1A),
)

private val NeonTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = 3.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = 4.sp),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = 2.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 2.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.5.sp),
)

private val NeonShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
)

/** The Termux Neon design system root. */
@Composable
fun NeonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) NeonDarkColorScheme else NeonDayColorScheme,
        typography = NeonTypography,
        shapes = NeonShapes,
        content = content,
    )
}