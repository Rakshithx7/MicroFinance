package com.example.microfinance.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandAccentLight
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.SplashGradientBottom
import com.example.microfinance.ui.theme.SplashGradientTop
import com.example.microfinance.ui.theme.TextOnPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    // Animation states
    val logoScale  = remember { Animatable(0.6f) }
    val logoAlpha  = remember { Animatable(0f) }
    val textAlpha  = remember { Animatable(0f) }
    val textOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        // Logo fade-in + scale
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(700)
            )
        }
        // Text appears slightly after logo
        delay(400)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(600)
            )
        }
        launch {
            textOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        }
        // Hold then navigate
        delay(1800)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SplashGradientTop, SplashGradientBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo mark ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                AppLogoMark(modifier = Modifier.size(64.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── App name ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(textAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mahila-Shakti Unnati",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextOnPrimary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Micro Finance",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = BrandAccentLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/**
 * Vector logo mark drawn with Canvas:
 * A stylised "M" monogram inside a soft circle, with a small upward
 * growth arrow accent — representing women's empowerment + financial growth.
 */
@Composable
fun AppLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
        val goldColor = BrandAccent
        val whiteColor = Color.White

        // ── Outer soft ring ───────────────────────────────────────────────
        drawCircle(
            color = whiteColor.copy(alpha = 0.15f),
            radius = w * 0.48f,
            style = Stroke(width = w * 0.04f)
        )

        // ── "M" letterform ────────────────────────────────────────────────
        val mPath = Path().apply {
            moveTo(w * 0.15f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.28f)
            lineTo(w * 0.50f, h * 0.58f)
            lineTo(w * 0.85f, h * 0.28f)
            lineTo(w * 0.85f, h * 0.75f)
        }
        drawPath(mPath, color = whiteColor, style = stroke)

        // ── Gold growth arrow (top-right accent) ──────────────────────────
        val arrowX = w * 0.72f
        val arrowY = h * 0.20f
        val arrowLen = w * 0.18f
        // Arrow shaft
        drawLine(
            color = goldColor,
            start = Offset(arrowX - arrowLen * 0.5f, arrowY + arrowLen * 0.5f),
            end   = Offset(arrowX + arrowLen * 0.5f, arrowY - arrowLen * 0.5f),
            strokeWidth = w * 0.055f,
            cap = StrokeCap.Round
        )
        // Arrow head
        drawLine(
            color = goldColor,
            start = Offset(arrowX + arrowLen * 0.5f, arrowY - arrowLen * 0.5f),
            end   = Offset(arrowX + arrowLen * 0.5f, arrowY + arrowLen * 0.1f),
            strokeWidth = w * 0.055f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = goldColor,
            start = Offset(arrowX + arrowLen * 0.5f, arrowY - arrowLen * 0.5f),
            end   = Offset(arrowX - arrowLen * 0.1f, arrowY - arrowLen * 0.5f),
            strokeWidth = w * 0.055f,
            cap = StrokeCap.Round
        )
    }
}
