package com.example.microfinance.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light colour scheme (only light mode for now — rural-friendly) ───────────
private val MahilaShaktiColorScheme = lightColorScheme(
    primary            = BrandPrimary,
    onPrimary          = TextOnPrimary,
    primaryContainer   = SurfaceElevated,
    onPrimaryContainer = BrandPrimaryDark,

    secondary          = BrandPrimaryLight,
    onSecondary        = TextOnPrimary,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,

    tertiary           = BrandAccent,
    onTertiary         = TextOnPrimary,
    tertiaryContainer  = BrandAccentLight,
    onTertiaryContainer = TextPrimary,

    background         = BackgroundLight,
    onBackground       = TextPrimary,

    surface            = SurfaceCard,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceElevated,
    onSurfaceVariant   = TextSecondary,

    outline            = BorderColor,
    outlineVariant     = DividerColor,

    error              = Color(0xFFB00020),
    onError            = Color.White,
)

@Composable
fun MicroFinanceTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Transparent status bar — content draws edge-to-edge
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = MahilaShaktiColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
