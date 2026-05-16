package com.example.microfinance.ui.member

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.data.entity.LoanWithRepaymentTotal
import com.example.microfinance.data.entity.MemberStatus
import com.example.microfinance.data.entity.SavingsEntryEntity
import com.example.microfinance.data.entity.SavingsStatus
import com.example.microfinance.ui.components.ChipStatus
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.PremiumCard
import com.example.microfinance.ui.components.SectionHeader
import com.example.microfinance.ui.components.StatusChip
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
import com.example.microfinance.ui.theme.SurfaceCard
import com.example.microfinance.ui.theme.SurfaceElevated
import com.example.microfinance.ui.theme.TextOnPrimary
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary
import com.example.microfinance.util.DateUtils

@Composable
fun MemberDetailScreen(
    viewModel: MemberViewModel,
    onBack: () -> Unit
) {
    val member       by viewModel.detailMember.collectAsState()
    val savings      by viewModel.detailSavingsEntries.collectAsState()
    val totalPaid    by viewModel.detailTotalPaid.collectAsState()
    val totalPending by viewModel.detailTotalPending.collectAsState()
    val paidCount    by viewModel.detailPaidCount.collectAsState()
    val pendingCount by viewModel.detailPendingCount.collectAsState()
    val lastPaid     by viewModel.detailLastPaidDate.collectAsState()
    val loans        by viewModel.detailLoansWithTotals.collectAsState()
    val totalRepaid  by viewModel.detailTotalRepaid.collectAsState()

    val currentMember = member ?: return

    val status = viewModel.getMemberStatus(paidCount, pendingCount)
    val (statusColor, statusBg, statusLabel, statusIcon) = when (status) {
        MemberStatus.GOOD_CONTRIBUTOR -> Quad(StatusPaid,    StatusPaidBg,    "Good Contributor", Icons.Rounded.Star)
        MemberStatus.ACTIVE           -> Quad(BrandPrimary,  BrandPrimary.copy(alpha = 0.10f), "Active", Icons.Rounded.CheckCircle)
        MemberStatus.IRREGULAR        -> Quad(BrandAccent,   BrandAccent.copy(alpha = 0.12f),  "Irregular", Icons.Rounded.Warning)
        MemberStatus.PENDING          -> Quad(StatusPending, StatusPendingBg, "Pending", Icons.Rounded.Warning)
    }

    val activeLoans = loans.filter { !it.isClosed }
    val closedLoans = loans.filter { it.isClosed }
    val totalLoanAmount = loans.sumOf { it.principalAmount }
    val totalInterest   = closedLoans.sumOf { it.interestAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Hero header ───────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(BrandPrimaryDark, BrandPrimary)))
                    .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    // Back button
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back", tint = TextOnPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MemberAvatar(name = currentMember.name, size = 60.dp)
                        Column {
                            Text(
                                text = currentMember.name,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = TextOnPrimary
                            )
                            if (currentMember.phone.isNotBlank()) {
                                Text(
                                    text = currentMember.phone,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 13.sp,
                                    color = TextOnPrimary.copy(alpha = 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Status badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(12.dp))
                                Text(statusLabel, color = statusColor, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = TextOnPrimary.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))
                    // Quick stats
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        HeroStat("Joined", DateUtils.formatDate(currentMember.createdAtMillis))
                        HeroStat("Last Payment", DateUtils.formatDateOrNull(lastPaid))
                        HeroStat("Active Loans", "${activeLoans.size}")
                    }
                }
            }
        }

        // ── Financial summary ─────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Financial Summary")
                Spacer(modifier = Modifier.height(8.dp))
                PremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailStat("Total Paid", "₹${String.format("%.0f", totalPaid)}", StatusPaid, StatusPaidBg, Modifier.weight(1f))
                            DetailStat("Pending", "₹${String.format("%.0f", totalPending)}", if (totalPending > 0) StatusPending else TextSecondary, if (totalPending > 0) StatusPendingBg else SurfaceElevated, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailStat("Weeks Paid", "$paidCount", BrandPrimary, BrandPrimary.copy(alpha = 0.08f), Modifier.weight(1f))
                            DetailStat("Weeks Missed", "$pendingCount", if (pendingCount > 0) StatusPending else TextSecondary, if (pendingCount > 0) StatusPendingBg else SurfaceElevated, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailStat("Total Loans", "₹${String.format("%.0f", totalLoanAmount)}", BrandAccent, BrandAccent.copy(alpha = 0.10f), Modifier.weight(1f))
                            DetailStat("Total Repaid", "₹${String.format("%.0f", totalRepaid)}", StatusPaid, StatusPaidBg, Modifier.weight(1f))
                        }

                        // Consistency bar
                        val total = paidCount + pendingCount
                        if (total > 0) {
                            val consistency = paidCount.toFloat() / total
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Payment Consistency", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
                                    Text("${(consistency * 100).toInt()}%", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = statusColor)
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)).background(SurfaceElevated)) {
                                    Box(modifier = Modifier.fillMaxWidth(consistency.coerceIn(0f, 1f)).height(6.dp).clip(RoundedCornerShape(50.dp)).background(statusColor))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Loan history ──────────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Loan History (${loans.size})")
            }
        }

        if (loans.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No loans yet", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                        }
                    }
                }
            }
        } else {
            items(loans) { loan ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MemberLoanCard(loan = loan)
                }
            }
        }

        // ── Savings history ───────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader(title = "Savings History (${savings.size})")
            }
        }

        if (savings.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No savings entries yet", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                        }
                    }
                }
            }
        } else {
            itemsIndexed(savings) { index, entry ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(150, delayMillis = index * 20))
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MemberSavingsCard(entry = entry)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextOnPrimary.copy(alpha = 0.6f))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextOnPrimary)
    }
}

@Composable
private fun DetailStat(label: String, value: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = color.copy(alpha = 0.75f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
private fun MemberLoanCard(loan: LoanWithRepaymentTotal) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("₹${String.format("%.2f", loan.principalAmount)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text("Issued: ${DateUtils.formatDate(loan.startDateMillis)}", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
                }
                StatusChip(status = if (loan.isClosed) ChipStatus.CLOSED else ChipStatus.PENDING)
            }
            HorizontalDivider(color = DividerColor)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniInfoCell("Rate", "${loan.interestRatePercent}%", Modifier.weight(1f))
                MiniInfoCell("Interest", "₹${String.format("%.0f", loan.interestAmount)}", Modifier.weight(1f))
                MiniInfoCell("Total Due", "₹${String.format("%.0f", loan.totalDue)}", Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniInfoCell("Repaid", "₹${String.format("%.0f", loan.totalRepaid)}", Modifier.weight(1f))
                MiniInfoCell("Outstanding", "₹${String.format("%.0f", loan.outstanding)}", Modifier.weight(1f))
                if (loan.dueDateMillis > 0) MiniInfoCell("Due Date", DateUtils.formatDate(loan.dueDateMillis), Modifier.weight(1f))
            }
            // Progress bar
            val progress = loan.repaymentProgressPercent
            Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50.dp)).background(SurfaceElevated)) {
                Box(modifier = Modifier.fillMaxWidth(progress).height(5.dp).clip(RoundedCornerShape(50.dp)).background(if (loan.isClosed) StatusPaid else BrandPrimary))
            }
            Text("${(progress * 100).toInt()}% repaid", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun MiniInfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceElevated).padding(6.dp)) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 9.sp, color = TextSecondary)
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = TextPrimary)
    }
}

@Composable
private fun MemberSavingsCard(entry: SavingsEntryEntity) {
    val chipStatus  = if (entry.status == SavingsStatus.PAID) ChipStatus.PAID else ChipStatus.PENDING
    val amountColor = if (entry.status == SavingsStatus.PAID) StatusPaid else StatusPending

    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("₹${String.format("%.2f", entry.amount)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = amountColor)
                Text(DateUtils.formatDate(entry.entryDateMillis), fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
                if (entry.weekNumber > 0) {
                    Text("Week ${entry.weekNumber}, ${entry.weekYear}", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
                }
            }
            StatusChip(status = chipStatus)
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
