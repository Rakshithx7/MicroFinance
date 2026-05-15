package com.example.microfinance.ui.auth

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.auth.AuthViewModel
import com.example.microfinance.auth.SyncState
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.PremiumCard
import com.example.microfinance.ui.components.PrimaryButton
import com.example.microfinance.ui.theme.BackgroundLight
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.BrandPrimaryDark
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.StatusPaid
import com.example.microfinance.ui.theme.StatusPaidBg
import com.example.microfinance.ui.theme.StatusPending
import com.example.microfinance.ui.theme.StatusPendingBg
import com.example.microfinance.ui.theme.SurfaceElevated
import com.example.microfinance.ui.theme.TextOnPrimary
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onSignedOut: () -> Unit
) {
    val syncState by authViewModel.syncState.collectAsState()
    val userName  = authViewModel.getUserName() ?: "User"
    val userEmail = authViewModel.getUserEmail() ?: ""
    val userId    = authViewModel.getUserId() ?: ""

    var showSignOutDialog by remember { mutableStateOf(false) }

    // Auto-clear sync success message after 3 seconds
    LaunchedEffect(syncState) {
        if (syncState is SyncState.Success) {
            kotlinx.coroutines.delay(3_000)
            authViewModel.clearSyncState()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ── Profile hero ──────────────────────────────────────────────────
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth(), elevation = 3.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(BrandPrimaryDark, BrandPrimary)))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MemberAvatar(name = userName, size = 64.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(userName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextOnPrimary)
                        Text(userEmail, fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = TextOnPrimary.copy(alpha = 0.75f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(StatusPaidBg)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = StatusPaid, modifier = Modifier.size(12.dp))
                            Text("Google Account Connected", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = StatusPaid)
                        }
                    }
                }
            }
        }

        // ── Sync status ───────────────────────────────────────────────────
        when (val state = syncState) {
            is SyncState.Syncing -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(BrandPrimary.copy(alpha = 0.08f)).padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(state.message, fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = BrandPrimary)
                        }
                    }
                }
            }
            is SyncState.Success -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(StatusPaidBg).padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = StatusPaid, modifier = Modifier.size(18.dp))
                            Text(state.message, fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = StatusPaid, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            is SyncState.Error -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(StatusPendingBg).padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.Error, null, tint = StatusPending, modifier = Modifier.size(18.dp))
                            Text(state.message, fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = StatusPending)
                        }
                    }
                }
            }
            else -> {}
        }

        // ── Account info ──────────────────────────────────────────────────
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Account Details", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    HorizontalDivider(color = DividerColor)
                    ProfileInfoRow(Icons.Rounded.Person, "Name", userName)
                    ProfileInfoRow(Icons.Rounded.Email, "Email", userEmail)
                    ProfileInfoRow(Icons.Rounded.Cloud, "User ID", userId.take(16) + "...")
                }
            }
        }

        // ── Cloud backup ──────────────────────────────────────────────────
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cloud Backup", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    HorizontalDivider(color = DividerColor)
                    Text(
                        text = "Your data is automatically backed up to the cloud whenever you add members, savings, loans, or repayments. You can also manually trigger a full backup below.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    PrimaryButton(
                        text  = "Backup Now",
                        onClick = { authViewModel.backupToCloud() },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.Backup,
                        enabled = syncState !is SyncState.Syncing
                    )
                }
            }
        }

        // ── Sign out ──────────────────────────────────────────────────────
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Account", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    HorizontalDivider(color = DividerColor)
                    Text(
                        text = "Signing out will keep your local data intact. You can sign back in anytime to restore your cloud data.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(StatusPending.copy(alpha = 0.08f))
                    ) {
                        PrimaryButton(
                            text  = "Sign Out",
                            onClick = { showSignOutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Rounded.Logout
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out?", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold) },
            text  = { Text("Your local data will remain. Sign back in to restore cloud data.", fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    authViewModel.signOut()
                    onSignedOut()
                }) {
                    Text("Sign Out", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, color = StatusPending)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", fontFamily = PoppinsFontFamily, color = BrandPrimary)
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(BrandPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
            Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextPrimary)
        }
    }
}
