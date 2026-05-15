package com.example.microfinance.ui.loan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.data.entity.LoanEntity
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.data.entity.RepaymentEntity
import com.example.microfinance.ui.components.ChipStatus
import com.example.microfinance.ui.components.EmptyState
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.PremiumCard
import com.example.microfinance.ui.components.PremiumTextField
import com.example.microfinance.ui.components.PrimaryButton
import com.example.microfinance.ui.components.SectionHeader
import com.example.microfinance.ui.components.StatCard
import com.example.microfinance.ui.components.StatusChip
import com.example.microfinance.ui.theme.BackgroundLight
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.StatusClosed
import com.example.microfinance.ui.theme.StatusPaid
import com.example.microfinance.ui.theme.StatusPaidBg
import com.example.microfinance.ui.theme.StatusPending
import com.example.microfinance.ui.theme.StatusPendingBg
import com.example.microfinance.ui.theme.SurfaceCard
import com.example.microfinance.ui.theme.SurfaceElevated
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary
import com.example.microfinance.util.DateUtils

@Composable
fun LoanScreen(
    viewModel: LoanViewModel,
    modifier: Modifier = Modifier
) {
    val members               by viewModel.members.collectAsState()
    val loans                 by viewModel.loansForSelectedMember.collectAsState()
    val repayments            by viewModel.repaymentsForSelectedLoan.collectAsState()
    val message               by viewModel.message.collectAsState()
    val openLoansCount        by viewModel.openLoansCount.collectAsState()
    val closedLoansCount      by viewModel.closedLoansCount.collectAsState()
    val totalActiveLoanAmt    by viewModel.totalActiveLoanAmount.collectAsState()
    val totalOutstanding      by viewModel.totalOutstandingBalance.collectAsState()
    val totalInterestEarned   by viewModel.totalInterestEarned.collectAsState()
    val overdueCount          by viewModel.overdueLoansCount.collectAsState()
    val activeLoansWithTotals by viewModel.activeLoansWithTotals.collectAsState()
    val closedLoansWithTotals by viewModel.closedLoansWithTotals.collectAsState()

    // ✅ FIX: selectedLoan now comes from ViewModel StateFlow (reactive, never stale)
    val selectedLoan     by viewModel.selectedLoan.collectAsState()
    val totalRepaid      by viewModel.totalRepaidForSelectedLoan.collectAsState()
    val outstanding      by viewModel.outstandingForSelectedLoan.collectAsState()
    val totalDue         by viewModel.totalDueForSelectedLoan.collectAsState()

    var selectedMember by remember { mutableStateOf<MemberEntity?>(null) }
    var principalInput by remember { mutableStateOf("") }
    var rateInput      by remember { mutableStateOf("") }
    var durationInput  by remember { mutableStateOf("") }
    var repaymentInput by remember { mutableStateOf("") }

    var showActiveSheet   by remember { mutableStateOf(false) }
    var showClosedSheet   by remember { mutableStateOf(false) }
    var showExposureSheet by remember { mutableStateOf(false) }
    var showInterestSheet by remember { mutableStateOf(false) }

    val memberNameMap = remember(members) { members.associate { it.id to it.name } }

    // ── Bottom sheets ─────────────────────────────────────────────────────
    if (showActiveSheet) {
        ActiveLoansSheet(activeLoans = activeLoansWithTotals, memberNameMap = memberNameMap, onDismiss = { showActiveSheet = false })
    }
    if (showClosedSheet) {
        ClosedLoansSheet(closedLoans = closedLoansWithTotals, memberNameMap = memberNameMap, onDismiss = { showClosedSheet = false })
    }
    if (showExposureSheet) {
        LoanExposureSheet(activeLoans = activeLoansWithTotals, memberNameMap = memberNameMap, onDismiss = { showExposureSheet = false })
    }
    if (showInterestSheet) {
        InterestEarnedSheet(closedLoans = closedLoansWithTotals, memberNameMap = memberNameMap, onDismiss = { showInterestSheet = false })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ── Loan analytics header ─────────────────────────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Active Loans",  "$openLoansCount",                                   Icons.Rounded.CreditCard,    BrandPrimary,  Modifier.weight(1f), onClick = { showActiveSheet = true })
                StatCard("Closed Loans",  "$closedLoansCount",                                 Icons.Rounded.CheckCircle,   StatusPaid,    Modifier.weight(1f), onClick = { showClosedSheet = true })
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Outstanding",   "₹${String.format("%.0f", totalOutstanding)}",       Icons.Rounded.AccountBalance, StatusPending, Modifier.weight(1f), onClick = { showExposureSheet = true })
                StatCard("Interest Earned","₹${String.format("%.0f", totalInterestEarned)}",   Icons.Rounded.TrendingUp,    BrandAccent,   Modifier.weight(1f), onClick = { showInterestSheet = true })
            }
        }

        // Overdue alert
        if (overdueCount > 0) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(StatusPending.copy(alpha = 0.10f))
                        .border(1.dp, StatusPending.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Warning, null, tint = StatusPending, modifier = Modifier.size(18.dp))
                        Text("⚠️ $overdueCount overdue loan(s) need attention", fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = StatusPending, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Status message
        if (message != null) {
            item {
                AnimatedVisibility(visible = true, enter = fadeIn(tween(300))) {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BrandPrimary.copy(alpha = 0.08f)).padding(12.dp)) {
                        Text(message ?: "", fontFamily = PoppinsFontFamily, fontSize = 13.sp, color = BrandPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Select member
        item { SectionHeader(title = "Select Member") }

        if (members.isEmpty()) {
            item { EmptyState(icon = Icons.Rounded.Group, message = "No members yet.\nAdd members first from the Members tab.") }
        } else {
            items(members) { member ->
                val isSelected = selectedMember?.id == member.id
                LoanMemberCard(member = member, isSelected = isSelected, onClick = {
                    selectedMember = member
                    viewModel.selectMember(member.id)
                })
            }
        }

        // Add loan form
        selectedMember?.let { member ->
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item {
                PremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MemberAvatar(name = member.name, size = 36.dp)
                            Column {
                                Text("Issue New Loan", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                                Text(member.name, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                        HorizontalDivider(color = DividerColor)
                        PremiumTextField(value = principalInput, onValueChange = { principalInput = it }, label = "Loan Amount (₹)", keyboardType = KeyboardType.Decimal,
                            leadingIcon = { Icon(Icons.Rounded.AccountBalance, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) })
                        PremiumTextField(value = rateInput, onValueChange = { rateInput = it }, label = "Interest Rate % (simple)", keyboardType = KeyboardType.Decimal,
                            leadingIcon = { Icon(Icons.Rounded.Percent, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) })
                        PremiumTextField(value = durationInput, onValueChange = { durationInput = it }, label = "Duration (months, 0 = open-ended)", keyboardType = KeyboardType.Number,
                            leadingIcon = { Icon(Icons.Rounded.CreditCard, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) })

                        // Live interest preview
                        val p = principalInput.toDoubleOrNull() ?: 0.0
                        val r = rateInput.toDoubleOrNull() ?: 0.0
                        val t = durationInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        if (p > 0 && r > 0) {
                            val si = p * r * t / 100.0
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BrandPrimary.copy(alpha = 0.06f)).padding(10.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("SI = ₹${String.format("%.2f", p)} × ${r}% × ${t}mo / 100 = ₹${String.format("%.2f", si)}", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = BrandPrimary)
                                    Text("Total Due = ₹${String.format("%.2f", p + si)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BrandPrimary)
                                }
                            }
                        }

                        PrimaryButton(text = "Issue Loan", onClick = {
                            viewModel.createLoan(
                                memberId       = member.id,
                                principal      = principalInput.toDoubleOrNull() ?: 0.0,
                                interestRate   = rateInput.toDoubleOrNull() ?: 0.0,
                                durationMonths = durationInput.toIntOrNull() ?: 0
                            )
                            principalInput = ""; rateInput = ""; durationInput = ""
                        }, modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.Add)
                    }
                }
            }

            // Loans list
            if (loans.isNotEmpty()) {
                item { SectionHeader(title = "Loan History") }
                items(loans) { loan ->
                    val td         = viewModel.calculateTotalDue(loan)
                    val isSelected = selectedLoan?.id == loan.id
                    LoanCard(loan = loan, totalDue = td, isSelected = isSelected,
                        onClick = { viewModel.selectLoan(loan.id) })
                }
            } else {
                item { EmptyState(icon = Icons.Rounded.CreditCard, message = "No loans yet for ${member.name}.") }
            }

            // ✅ FIX: Repayment section uses REACTIVE selectedLoan from ViewModel
            // outstanding, totalRepaid, totalDue all update instantly via StateFlow
            selectedLoan?.let { loan ->
                item { Spacer(modifier = Modifier.height(4.dp)) }
                item {
                    PremiumCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Repayment", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)

                            // Interest breakdown
                            val interest = viewModel.calculateInterest(loan)
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BrandAccent.copy(alpha = 0.07f)).padding(10.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Principal: ₹${String.format("%.2f", loan.principalAmount)}", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
                                    Text("Interest (SI): ₹${String.format("%.2f", interest)}  (${loan.interestRatePercent}% × ${maxOf(loan.durationMonths, 1)}mo)", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = BrandAccent)
                                    Text("Total Due: ₹${String.format("%.2f", totalDue)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                                }
                            }

                            // ✅ These three values update INSTANTLY from reactive StateFlows
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RepaymentStat("Total Due",   "₹${String.format("%.2f", totalDue)}",    StatusPending, StatusPendingBg, Modifier.weight(1f))
                                RepaymentStat("Repaid",      "₹${String.format("%.2f", totalRepaid)}", StatusPaid,    StatusPaidBg,    Modifier.weight(1f))
                                RepaymentStat("Outstanding", "₹${String.format("%.2f", outstanding)}",
                                    if (outstanding > 0) StatusPending else StatusPaid,
                                    if (outstanding > 0) StatusPendingBg else StatusPaidBg,
                                    Modifier.weight(1f))
                            }

                            HorizontalDivider(color = DividerColor)

                            if (!loan.isClosed) {
                                PremiumTextField(value = repaymentInput, onValueChange = { repaymentInput = it }, label = "Repayment Amount (₹)", keyboardType = KeyboardType.Decimal,
                                    leadingIcon = { Icon(Icons.Rounded.AccountBalance, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) })
                                PrimaryButton(text = "Add Repayment", onClick = {
                                    viewModel.addRepayment(loanId = loan.id, amount = repaymentInput.toDoubleOrNull() ?: 0.0)
                                    repaymentInput = ""
                                }, modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.CheckCircle)
                            } else {
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(StatusPaidBg).padding(12.dp), contentAlignment = Alignment.Center) {
                                    Text("✅ Loan fully repaid and closed", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StatusPaid)
                                }
                            }
                        }
                    }
                }

                if (repayments.isNotEmpty()) {
                    item { SectionHeader(title = "Repayment History") }
                    items(repayments) { repayment -> RepaymentCard(repayment = repayment) }
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
private fun LoanMemberCard(member: MemberEntity, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) BrandPrimary else DividerColor
    val bgColor     = if (isSelected) BrandPrimary.copy(alpha = 0.05f) else SurfaceCard
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(remember { MutableInteractionSource() }, ripple(bounded = true)) { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MemberAvatar(name = member.name, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                if (member.phone.isNotBlank()) Text(member.phone, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary)
            }
            if (isSelected) Icon(Icons.Rounded.CheckCircle, null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun LoanCard(loan: LoanEntity, totalDue: Double, isSelected: Boolean, onClick: () -> Unit) {
    val chipStatus  = if (loan.isClosed) ChipStatus.CLOSED else ChipStatus.PENDING
    val borderColor = if (isSelected) BrandPrimary else DividerColor
    val now         = System.currentTimeMillis()
    val isOverdue   = !loan.isClosed && loan.dueDateMillis > 0 && loan.dueDateMillis < now

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceCard)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(remember { MutableInteractionSource() }, ripple(bounded = true)) { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("₹ ${String.format("%.2f", loan.principalAmount)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                    Text("Issued: ${DateUtils.formatDate(loan.startDateMillis)}", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(status = chipStatus)
                    if (isOverdue) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(StatusPending.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("OVERDUE", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = StatusPending)
                        }
                    }
                }
            }
            HorizontalDivider(color = DividerColor)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LoanDetail("Interest",  "${loan.interestRatePercent}%",                                                                    modifier = Modifier.weight(1f))
                LoanDetail("Total Due", "₹${String.format("%.2f", totalDue)}", valueColor = if (loan.isClosed) StatusClosed else StatusPending, modifier = Modifier.weight(1f))
                if (loan.dueDateMillis > 0) LoanDetail("Due Date", DateUtils.formatDate(loan.dueDateMillis), valueColor = if (isOverdue) StatusPending else TextPrimary, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LoanDetail(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = TextPrimary) {
    Column(modifier = modifier) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = valueColor)
    }
}

@Composable
private fun RepaymentStat(label: String, value: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = color.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun RepaymentCard(repayment: RepaymentEntity) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("₹ ${String.format("%.2f", repayment.amount)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = StatusPaid)
                Text(DateUtils.formatDateTime(repayment.paidAtMillis), fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
            }
            StatusChip(status = ChipStatus.PAID)
        }
    }
}
