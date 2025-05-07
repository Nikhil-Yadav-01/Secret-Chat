package com.rudraksha.secretchat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Create a custom color palette
data class SecretChatColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val background: Color,
    val backgroundDarker: Color,
    val surface: Color,
    val surfaceLighter: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val accent: Color,
    val accentSecondary: Color,
    val messageReceived: Color,
    val messageSent: Color,
    val messageSelfDestruct: Color
)

// Create a composition local to hold our colors
val LocalSecretChatColors = staticCompositionLocalOf {
    SecretChatColors(
        primary = DeepNavy,
        primaryVariant = DarkPurple,
        secondary = DarkTeal,
        background = BackgroundDark,
        backgroundDarker = BackgroundDarker,
        surface = SurfaceDark,
        surfaceLighter = SurfaceLighter,
        error = ErrorRed,
        onPrimary = White,
        onSecondary = White,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        onError = White,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textHint = TextHint,
        accent = GoldAccent,
        accentSecondary = MintGreen,
        messageReceived = ReceivedMessage,
        messageSent = SentMessage,
        messageSelfDestruct = SelfDestructMessage
    )
}

// Define the Material 3 color scheme
private val DarkColorScheme = darkColorScheme(
    primary = DeepNavy,
    onPrimary = White,
    primaryContainer = DarkPurple,
    onPrimaryContainer = White,
    secondary = DarkTeal,
    onSecondary = White,
    secondaryContainer = DarkTeal.copy(alpha = 0.7f),
    onSecondaryContainer = White,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    tertiaryContainer = GoldAccent.copy(alpha = 0.2f),
    onTertiaryContainer = GoldAccent,
    error = ErrorRed,
    onError = White,
    errorContainer = SelfDestructMessage,
    onErrorContainer = White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLighter,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary
)

@Composable
fun SecretChatTheme(
    darkTheme: Boolean = true, // Default to dark theme
    content: @Composable () -> Unit
) {
    // Always use dark theme for this app
    val colorScheme = DarkColorScheme
    val secretChatColors = SecretChatColors(
        primary = DeepNavy,
        primaryVariant = DarkPurple,
        secondary = DarkTeal,
        background = BackgroundDark,
        backgroundDarker = BackgroundDarker,
        surface = SurfaceDark,
        surfaceLighter = SurfaceLighter,
        error = ErrorRed,
        onPrimary = White,
        onSecondary = White,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        onError = White,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textHint = TextHint,
        accent = GoldAccent,
        accentSecondary = MintGreen,
        messageReceived = ReceivedMessage,
        messageSent = SentMessage,
        messageSelfDestruct = SelfDestructMessage
    )
    
    // Set up system UI colors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
//            window.statusBarColor = BackgroundDarker.toArgb()
//            window.navigationBarColor = BackgroundDarker.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    
    CompositionLocalProvider(
        LocalSecretChatColors provides secretChatColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

// Extension to access our custom colors easily
object SecretChatTheme {
    val colors: SecretChatColors
        @Composable
        get() = LocalSecretChatColors.current
}