package com.example.microfinance.ui.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.data.entity.LoanWithRepaymentTotal
import com.example.microfinance.ui.components.AnalyticsBottomSheet
import com.example.microfinance.ui.components.ChipStatus
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.StatusChip
import com.example.microfinance.ui.savings.SheetStat
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.StatusClosed
import com.example.microfinance.ui.theme.StatusClosedBg
import com.example.microfinance.ui.theme.StatusPaid
import com.example.microfinance.ui.theme.StatusPaidBg
import com.example.microfinance.ui.theme.StatusPending
import com.example.microfinance.ui.theme.StatusPendingBg
import com.example.microfinance.ui.theme.SurfaceElevated
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary
import com.example.microfinance.util.DateUtils

// ─────────────────────────────────────────────────────────────────────────────
// 1. Active Loans drill-down
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveLoansSheet(
    activeLoans: List<LoanWithRepaymentTotal>,
    memberNameMap: Map<Long, String>,
    onDismiss: () -> Unit
) {
    val totalExposure = activeLoans.sumOf { it.principalAmount }
    val totalOutstanding = activeLoans.sumOf { it.outstanding }

    AnalyticsBottomSheet(
        title    = "Active Loans",
        subtitle = "${activeLoans.size} open loans",
        icon     = Icons.Rounded.CreditCard,
        iconTint = BrandPrimary,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetStat("Exposure", "₹${String.format("%.0f", totalExposure)}", StatusPending, StatusPendingBg, Modifier.weight(1f))
                    SheetStat("Outstanding", "₹${String.format("%.0f", totalOutstanding)}", BrandAccent, BrandAccent.copy(alpha = 0.10f), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (activeLoans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active loans", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                items(activeLoans) { loan ->
                    LoanDetailRow(loan = loan, memberName = memberNameMap[loan.memberId] ?: "Member")
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Closed Loans drill-down
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosedLoansSheet(
    closedLoans: List<LoanWithRepaymentTotal>,
    memberNameMap: Map<Long, String>,
    onDismiss: () -> Unit
) {
    val totalRepaid   = closedLoans.sumOf { it.totalRepaid }
    val totalInterest = closedLoans.sumOf { it.interestAmount }

    AnalyticsBottomSheet(
        title    = "Closed Loans",
        subtitle = "${closedLoans.size} completed loans",
        icon     = Icons.Rounded.CheckCircle,
        iconTint = StatusPaid,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetStat("Total Repaid", "₹${String.format("%.0f", totalRepaid)}", StatusPaid, StatusPaidBg, Modifier.weight(1f))
                    SheetStat("Interest Earned", "₹${String.format("%.0f", totalInterest)}", BrandAccent, BrandAccent.copy(alpha = 0.10f), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (closedLoans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No closed loans yet", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                items(closedLoans) { loan ->
                    LoanDetailRow(loan = loan, memberName = memberNameMap[loan.memberId] ?: "Member")
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Loan Exposure drill-down
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanExposureSheet(
    activeLoans: List<LoanWithRepaymentTotal>,
    memberNameMap: Map<Long, String>,
    onDismiss: () -> Unit
) {
    val totalExposure    = activeLoans.sumOf { it.principalAmount }
    val totalOutstanding = activeLoans.sumOf { it.outstanding }
    val totalRepaid      = activeLoans.sumOf { it.totalRepaid }

    AnalyticsBottomSheet(
        title    = "Loan Exposure",
        subtitle = "₹${String.format("%.2f", totalExposure)} active principal",
        icon     = Icons.Rounded.AccountBalance,
        iconTint = StatusPending,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetStat("Principal Out", "₹${String.format("%.0f", totalExposure)}", StatusPending, StatusPendingBg, Modifier.weight(1f))
                    SheetStat("Outstanding", "₹${String.format("%.0f", totalOutstanding)}", BrandAccent, BrandAccent.copy(alpha = 0.10f), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                SheetStat("Repaid So Far", "₹${String.format("%.0f", totalRepaid)}", StatusPaid, StatusPaidBg, Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(activeLoans) { loan ->
                LoanExposureRow(loan = loan, memberName = memberNameMap[loan.memberId] ?: "Member")
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LoanExposureRow(loan: LoanWithRepaymentTotal, memberName: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MemberAvatar(name = memberName, size = 36.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(memberName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Text("Principal: ₹${String.format("%.0f", loan.principalAmount)} @ ${loan.interestRatePercent}%", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${String.format("%.0f", loan.outstanding)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (loan.outstanding > 0) StatusPending else StatusPaid)
                Text("outstanding", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Repayment progress bar
        val progress = loan.repaymentProgressPercent
        Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50.dp)).background(SurfaceElevated)) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(5.dp).clip(RoundedCornerShape(50.dp)).background(StatusPaid))
        }
        Text("${(progress * 100).toInt()}% repaid", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
    }
    HorizontalDivider(color = DividerColor)
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. Interest Earned drill-down
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestEarnedSheet(
    closedLoans: List<LoanWithRepaymentTotal>,
    memberNameMap: Map<Long, String>,
    onDismiss: () -> Unit
) {
    val totalInterest = closedLoans.sumOf { it.interestAmount }

    AnalyticsBottomSheet(
        title    = "Interest Earned",
        subtitle = "₹${String.format("%.2f", totalInterest)} from closed loans",
        icon     = Icons.Rounded.TrendingUp,
        iconTint = BrandAccent,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetStat("Total Interest", "₹${String.format("%.0f", totalInterest)}", BrandAccent, BrandAccent.copy(alpha = 0.10f), Modifier.weight(1f))
                    SheetStat("Closed Loans", "${closedLoans.size}", StatusPaid, StatusPaidBg, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (closedLoans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No interest earned yet", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                items(closedLoans) { loan ->
                    InterestRow(loan = loan, memberName = memberNameMap[loan.memberId] ?: "Member")
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun InterestRow(loan: LoanWithRepaymentTotal, memberName: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MemberAvatar(name = memberName, size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(memberName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Text("₹${String.format("%.0f", loan.principalAmount)} @ ${loan.interestRatePercent}% × ${maxOf(loan.durationMonths, 1)}mo", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
            Text("Closed: ${DateUtils.formatDate(loan.startDateMillis)}", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
        }
        Text("₹${String.format("%.0f", loan.interestAmount)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandAccent)
    }
    HorizontalDivider(color = DividerColor)
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared loan detail row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoanDetailRow(loan: LoanWithRepaymentTotal, memberName: String) {
    val chipStatus = if (loan.isClosed) ChipStatus.CLOSED else if (loan.isOverdue) ChipStatus.PENDING else ChipStatus.PENDING
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MemberAvatar(name = memberName, size = 36.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(memberName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Text("₹${String.format("%.0f", loan.principalAmount)} @ ${loan.interestRatePercent}%", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
                if (loan.dueDateMillis > 0) {
                    Text("Due: ${DateUtils.formatDate(loan.dueDateMillis)}", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = if (loan.isOverdue) StatusPending else TextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status = if (loan.isClosed) ChipStatus.CLOSED else ChipStatus.PENDING)
                if (loan.isOverdue) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("OVERDUE", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = StatusPending)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniLoanStat("Total Due", "₹${String.format("%.0f", loan.totalDue)}", StatusPending, Modifier.weight(1f))
            MiniLoanStat("Repaid", "₹${String.format("%.0f", loan.totalRepaid)}", StatusPaid, Modifier.weight(1f))
            MiniLoanStat("Outstanding", "₹${String.format("%.0f", loan.outstanding)}", if (loan.outstanding > 0) BrandAccent else StatusPaid, Modifier.weight(1f))
        }
    }
    HorizontalDivider(color = DividerColor)
}

@Composable
private fun MiniLoanStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.07f)).padding(6.dp)) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 9.sp, color = color.copy(alpha = 0.8f))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color)
    }
}
