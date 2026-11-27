package com.chowdhurymills.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chowdhurymills.android.R

val Olive = Color(0xFF1B3328)
val Olive2 = Color(0xFF2C5340)
val Gold = Color(0xFFB8860B)
val Gold2 = Color(0xFFE6C36A)
val Paper = Color(0xFFF3EEE4)
val Card = Color(0xFFFFFDF8)
val Line = Color(0xFFE4D8C4)
val Ink = Color(0xFF1C1914)
val Muted = Color(0xFF6D6458)
val Cream = Color(0xFFF7F1E4)
val Danger = Color(0xFF8D342C)
val ReceiptBg = Color(0xFFEEF6F0)
val PaymentBg = Color(0xFFFBF3EE)

private val Serif = FontFamily(Font(R.font.fraunces_semibold, FontWeight.SemiBold))
private val Sans = FontFamily(Font(R.font.figtree_regular, FontWeight.Normal), Font(R.font.figtree_bold, FontWeight.Bold))

private val LightColors = lightColorScheme(
    primary = Olive,
    onPrimary = Color(0xFFF6F1E7),
    secondary = Gold,
    onSecondary = Olive,
    background = Paper,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    error = Danger,
    outline = Line,
)

@Composable
fun MillTheme(content: @Composable () -> Unit) {
    // Product branding is light paper + olive; ignore system dark preference.
    @Suppress("UNUSED_VARIABLE")
    val unusedDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, color = Ink, lineHeight = 44.sp),
            headlineLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, color = Ink),
            headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Ink),
            titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Ink),
            bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Ink, lineHeight = 22.sp),
            bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = Muted, lineHeight = 20.sp),
            labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink),
            labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Gold, letterSpacing = 1.2.sp),
        ),
        content = content,
    )
}
