package com.example.projeto_ttc2.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = TealGreen,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightTeal,
    surface = LightTeal,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText,
)


val ColorScheme.heartRateCard: Color @Composable
get() = if (isSystemInDarkTheme()) HeartRateRed else HeartRateRed

val ColorScheme.stepsCard: Color @Composable
get() = if (isSystemInDarkTheme()) StepsBlue else StepsBlue

val ColorScheme.caloriesCard: Color @Composable
get() = if (isSystemInDarkTheme()) CaloriesOrange else CaloriesOrange

val ColorScheme.sleepCard: Color @Composable
get() = if (isSystemInDarkTheme()) SleepGreen else SleepGreen

val ColorScheme.defaultCard: Color @Composable
get() = if (isSystemInDarkTheme()) DefaultCardColor else DefaultCardColor


@Composable
fun ProjetoTTC2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}