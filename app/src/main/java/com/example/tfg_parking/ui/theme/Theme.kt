package com.example.tfg_parking.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Accent palettes ────────────────────────────────────────────────────────
private fun accentLight(primary: Color, secondary: Color) = lightColorScheme(
    primary   = primary,
    secondary = secondary,
    tertiary  = Pink40
)
private fun accentDark(primary: Color, secondary: Color) = darkColorScheme(
    primary   = primary,
    secondary = secondary,
    tertiary  = Pink80
)

val AccentBlueLight   = accentLight(Purple40,            PurpleGrey40)
val AccentBlueDark    = accentDark (Purple80,            PurpleGrey80)
val AccentGreenLight  = accentLight(Color(0xFF2E7D32),   Color(0xFF388E3C))
val AccentGreenDark   = accentDark (Color(0xFF81C784),   Color(0xFFA5D6A7))
val AccentOrangeLight = accentLight(Color(0xFFE65100),   Color(0xFFBF360C))
val AccentOrangeDark  = accentDark (Color(0xFFFFB74D),   Color(0xFFFFCC80))
val AccentRedLight    = accentLight(Color(0xFFC62828),   Color(0xFFB71C1C))
val AccentRedDark     = accentDark (Color(0xFFEF9A9A),   Color(0xFFFFCDD2))

fun colorSchemeForAccent(accent: String, dark: Boolean): ColorScheme = when (accent) {
    "green"  -> if (dark) AccentGreenDark  else AccentGreenLight
    "orange" -> if (dark) AccentOrangeDark else AccentOrangeLight
    "red"    -> if (dark) AccentRedDark    else AccentRedLight
    else     -> if (dark) AccentBlueDark   else AccentBlueLight
}

// ── Theme composable ───────────────────────────────────────────────────────
@Composable
fun TFGParkingTheme(
    darkTheme:    Boolean = isSystemInDarkTheme(),
    accentColor:  String  = "blue",
    dynamicColor: Boolean = false,          // desactivado para que funcione el modo oscuro propio
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        else -> colorSchemeForAccent(accentColor, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}