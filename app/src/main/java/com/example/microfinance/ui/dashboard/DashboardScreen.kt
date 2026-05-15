package com.example.microfinance.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.data.entity.MemberSavingsTotal
import com.example.microfinance.data.entity.MemberStatus
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.PremiumCard
import com.example.microfinance.ui.components.PrimaryButton
import com.example.microfinance.ui.components.SectionHeader
import com.example.microfinance.ui.components.StatCard
import com.example.microfinance.ui.theme.BackgroundLight
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandAccentLight
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.BrandPrimaryDark
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.StatusClosed
import com.example.microfinance.ui.theme.StatusClosedBg
import com.example.microfinance.ui.theme.StatusPaid
import com.example.microfinance.ui.theme.StatusPaidBg
import com.example.microfinance.ui.theme.StatusPending
import com.example.microfinance.ui.theme.StatusPendingBg
import com.example.microfinance.ui.theme.SurfaceCard
import com.example.microfinance.ui.theme.SurfaceElevated
import com.example.microfinance.ui.theme.TextOnPrimary
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary
import com.example.microfinance.util.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val totalPaid            by viewModel.totalPaid.collectAsState()
    val totalPending         by viewModel.totalPending.collectAsState()
    val memberTotals         by viewModel.memberTotals.collectAsState()
    val openLoansCount       by viewModel.openLoansCount.collectAsState()
    val closedLoansCount     by viewModel.closedLoansCount.collectAsState()
    val totalActiveLoanAmt   by viewModel.totalActiveLoanAmount.collectAsState()
    val totalInterestEarned  by viewModel.totalInterestEarned.collectAsState()
    val groupCapital         by viewModel.groupCapital.collectAsState()
    val recoveryRate         by viewModel.recoveryRate.collectAsState()
    val totalMembers         by viewModel.totalMembersCount.collectAsState()
    val membersPaidThisWeek  by viewModel.membersPaidThisWeek.collectAsState()
    val membersPendingWeek   by viewModel.membersPendingThisWeek.collectAsState()
    val weeklyCollection     by viewModel.weeklyCollection.collectAsState()
    val totalGroupRepaid     by viewModel.totalGroupRepaid.collectAsState()
    val membersWithPending   by viewModel.membersWithPending.collectAsState()
    val highRiskMembers      by viewModel.highRiskMembers.collectAsState()
    val overdueCount         by viewModel.overdueLoansCount.collectAsState()
    val groupSettings        by viewModel.groupSettings.collectAsState()

    val context      = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ── Hero gradient banner ──────────────────────────────────────────
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandPrimaryDark, BrandPrimary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = groupSettings?.groupName ?: "Mahila-Shakti Unnati",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextOnPrimary.copy(alpha = 0.75f),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "₹ ${String.format("%.2f", groupCapital)}",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp,
                            color = TextOnPrimary
                        )
                        Text(
                            text = "Group Capital Available",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = TextOnPrimary.copy(alpha = 0.65f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = TextOnPrimary.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            HeroBannerStat("Savings", "₹${String.format("%.0f", totalPaid)}")
                            HeroBannerStat("Interest", "₹${String.format("%.0f", totalInterestEarned)}")
                            HeroBannerStat("Loans Out", "₹${String.format("%.0f", totalActiveLoanAmt)}")
                        }
                    }
                }
            }
        }

        // ── Alerts ────────────────────────────────────────────────────────
        if (overdueCount > 0 || highRiskMembers.isNotEmpty() || membersWithPending.isNotEmpty()) {
            item {
                AlertsSection(
                    overdueCount      = overdueCount,
                    highRiskCount     = highRiskMembers.size,
                    pendingMemberCount = membersWithPending.size
                )
            }
        }

        // ── Key metrics row 1 ─────────────────────────────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Total Members",  "$totalMembers",                                    Icons.Rounded.Group,         BrandPrimary,  Modifier.weight(1f))
                StatCard("Pending Dues",   "₹${String.format("%.0f", totalPending)}",          Icons.Rounded.TrendingDown,  StatusPending, Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Active Loans",   "$openLoansCount",                                  Icons.Rounded.CreditCard,    BrandAccent,   Modifier.weight(1f))
                StatCard("Closed Loans",   "$closedLoansCount",                                Icons.Rounded.CheckCircle,   StatusPaid,    Modifier.weight(1f))
            }
        }
        item {
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "This Week",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    HorizontalDivider(color = DividerColor)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WeekStat("Collected",    "₹${String.format("%.0f", weeklyCollection)}",  StatusPaid,    StatusPaidBg,    Modifier.weight(1f))
                        WeekStat("Paid",         "$membersPaidThisWeek members",                  BrandPrimary,  BrandPrimary.copy(alpha = 0.08f), Modifier.weight(1f))
                        WeekStat("Pending",      "$membersPendingWeek members",                   StatusPending, StatusPendingBg, Modifier.weight(1f))
                    }
                }
            }
        }

        // ── Recovery & repayment ──────────────────────────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Recovery Rate",  "${String.format("%.1f", recoveryRate)}%",           Icons.Rounded.TrendingUp,    StatusPaid,    Modifier.weight(1f))
                StatCard("Total Repaid",   "₹${String.format("%.0f", totalGroupRepaid)}",       Icons.Rounded.AccountBalance, BrandPrimary, Modifier.weight(1f))
            }
        }

        // ── Share button ──────────────────────────────────────────────────
        item {
            PrimaryButton(
                text  = "Share Summary via WhatsApp",
                onClick = {
                    coroutineScope.launch {
                        val summary = viewModel.buildSummaryText()
                        ShareUtils.shareText(context, summary)
                    }
                },
                modifier    = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Rounded.IosShare
            )
        }

        // ── High-risk members ─────────────────────────────────────────────
        if (highRiskMembers.isNotEmpty()) {
            item { SectionHeader(title = "⚠️ High-Risk Members") }
            itemsIndexed(highRiskMembers) { index, item ->
                MemberRankCard(
                    item     = item,
                    index    = index,
                    status   = viewModel.computeMemberStatus(item.paidCount, item.pendingCount),
                    isRisk   = true
                )
            }
        }

        // ── Member savings ranking ────────────────────────────────────────
        item { SectionHeader(title = "Member Savings Ranking") }

        if (memberTotals.isEmpty()) {
            item {
                PremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.BarChart, null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                        Text("No savings data yet", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            itemsIndexed(memberTotals) { index, item ->
                AnimatedVisibility(
                    visible = true,
                    enter   = fadeIn(tween(200, delayMillis = index * 50)) +
                              slideInVertically(tween(200, delayMillis = index * 50)) { it / 3 }
                ) {
                    MemberRankCard(
                        item   = item,
                        index  = index,
                        status = viewModel.computeMemberStatus(item.paidCount, item.pendingCount),
                        isRisk = false
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroBannerStat(label: String, value: String) {
    Column {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextOnPrimary.copy(alpha = 0.6f))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextOnPrimary)
    }
}

@Composable
private fun WeekStat(label: String, value: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = color.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun AlertsSection(overdueCount: Int, highRiskCount: Int, pendingMemberCount: Int) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Alerts",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            HorizontalDivider(color = DividerColor)

            if (overdueCount > 0) {
                AlertRow(
                    icon    = Icons.Rounded.Warning,
                    color   = StatusPending,
                    message = "$overdueCount loan(s) are overdue"
                )
            }
            if (highRiskCount > 0) {
                AlertRow(
                    icon    = Icons.Rounded.Warning,
                    color   = BrandAccent,
                    message = "$highRiskCount member(s) are irregular/high-risk"
                )
            }
            if (pendingMemberCount > 0) {
                AlertRow(
                    icon    = Icons.Rounded.TrendingDown,
                    color   = StatusPending,
                    message = "$pendingMemberCount member(s) have unpaid savings"
                )
            }
        }
    }
}

@Composable
private fun AlertRow(icon: ImageVector, color: Color, message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.07f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(message, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MemberRankCard(
    item: MemberSavingsTotal,
    index: Int,
    status: MemberStatus,
    isRisk: Boolean
) {
    val (statusColor, statusBg, statusLabel) = when (status) {
        MemberStatus.GOOD_CONTRIBUTOR -> Triple(StatusPaid,    StatusPaidBg,    "Good Contributor")
        MemberStatus.ACTIVE           -> Triple(BrandPrimary,  BrandPrimary.copy(alpha = 0.10f), "Active")
        MemberStatus.IRREGULAR        -> Triple(BrandAccent,   BrandAccent.copy(alpha = 0.12f),  "Irregular")
        MemberStatus.PENDING          -> Triple(StatusPending, StatusPendingBg, "Pending")
    }

    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank / risk badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isRisk) StatusPendingBg else SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (isRisk) {
                    Icon(Icons.Rounded.Warning, null, tint = StatusPending, modifier = Modifier.size(14.dp))
                } else {
                    Text(
                        text = "${index + 1}",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            MemberAvatar(name = item.memberName, size = 40.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(item.memberName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(statusLabel, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = statusColor)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.0f", item.totalPaid)}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = StatusPaid
                )
                if (item.totalPending > 0) {
                    Text(
                        text = "−₹${String.format("%.0f", item.totalPending)}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = StatusPending
                    )
                }
            }
        }
    }
}
