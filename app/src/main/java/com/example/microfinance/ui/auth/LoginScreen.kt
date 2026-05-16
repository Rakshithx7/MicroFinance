package com.example.microfinance.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.auth.AuthState
import com.example.microfinance.auth.AuthViewModel
import com.example.microfinance.ui.components.PrimaryButton
import com.example.microfinance.ui.splash.AppLogoMark
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandAccentLight
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.BrandPrimaryDark
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.StatusPending
import com.example.microfinance.ui.theme.SurfaceCard
import com.example.microfinance.ui.theme.TextOnPrimary
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onSignedIn: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    // Navigate when signed in
    LaunchedEffect(authState) {
        if (authState is AuthState.SignedIn) onSignedIn()
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandPrimaryDark, BrandPrimary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ── Logo ──────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    AppLogoMark(modifier = Modifier.size(64.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── App name ──────────────────────────────────────────────
                Text(
                    text = "Mahila-Shakti Unnati",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = TextOnPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Micro Finance",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = BrandAccentLight,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // ── Feature highlights ────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FeatureRow(Icons.Rounded.Cloud,    "Cloud Backup",    "Your data is safe even if you change phones")
                        FeatureRow(Icons.Rounded.Refresh,  "Auto Restore",    "Login on any device to get all your data back")
                        FeatureRow(Icons.Rounded.Security, "Secure & Private","Only you can access your group's data")
                        FeatureRow(Icons.Rounded.Lock,     "Offline First",   "Works without internet, syncs when connected")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Sign-in button / loading ──────────────────────────────
                when (authState) {
                    is AuthState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = TextOnPrimary, modifier = Modifier.size(36.dp))
                            Text("Signing in...", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextOnPrimary.copy(alpha = 0.75f))
                        }
                    }
                    is AuthState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(StatusPending.copy(alpha = 0.15f)).padding(12.dp)
                            ) {
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                            GoogleSignInButton(onClick = { authViewModel.signInWithGoogle() })
                        }
                    }
                    else -> {
                        GoogleSignInButton(onClick = { authViewModel.signInWithGoogle() })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "By signing in, you agree to keep your group's financial data secure.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = TextOnPrimary.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .padding(0.dp)
    ) {
        PrimaryButton(
            text  = "Continue with Google",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = Icons.Rounded.AccountCircle
        )
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = BrandAccentLight, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(title, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextOnPrimary)
            Text(subtitle, fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextOnPrimary.copy(alpha = 0.65f))
        }
    }
}
