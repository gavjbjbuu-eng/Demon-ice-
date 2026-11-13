package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val IceEsportsColorScheme = lightColorScheme(
    primary = RoyalPurple,
    onPrimary = PureWhite,
    primaryContainer = SoftLavender,
    onPrimaryContainer = DeepPurple,
    secondary = EsportsYellow,
    onSecondary = DarkInk,
    secondaryContainer = SoftYellowBg,
    onSecondaryContainer = DarkInk,
    tertiary = ElectricPurple,
    onTertiary = PureWhite,
    background = OffWhite,
    onBackground = DarkInk,
    surface = PureWhite,
    onSurface = DarkInk,
    surfaceVariant = SoftLavender,
    onSurfaceVariant = MutedPurple,
    outline = CardLavenderBorder,
    error = AlertBlockedRed,
    onError = PureWhite,
)

private val IceEsportsShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun IceEsportsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IceEsportsColorScheme,
        typography = Typography,
        shapes = IceEsportsShapes,
        content = content
    )
}
