package com.example.microfinance.ui.savings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Star
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
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.data.entity.MemberStatus
import com.example.microfinance.data.entity.SavingsEntryEntity
import com.example.microfinance.data.entity.SavingsStatus
import com.example.microfinance.ui.components.ChipStatus
import com.example.microfinance.ui.components.EmptyState
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.PremiumCard
import com.example.microfinance.ui.components.PremiumTextField
import com.example.microfinance.ui.components.PrimaryButton
import com.example.microfinance.ui.components.SectionHeader
import com.example.microfinance.ui.components.StatusChip
import com.example.microfinance.ui.components.StatusToggle
import com.example.microfinance.ui.theme.BackgroundLight
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.PoppinsFontFamily
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
fun SavingsScreen(
    viewModel: SavingsViewModel,
    modifier: Modifier = Modifier
) {
    val members             by viewModel.members.collectAsState()
    val savingsEntries      by viewModel.savingsForSelectedMember.collectAsState()
    val totalPaid           by viewModel.totalPaidForSelected.collectAsState()
    val totalPending        by viewModel.totalPendingForSelected.collectAsState()
    val paidCount           by viewModel.paidCountForSelected.collectAsState()
    val pendingCount        by viewModel.pendingCountForSelected.collectAsState()
    val lastPaidDate        by viewModel.lastPaidDateForSelected.collectAsState()
    val selectedMemberId    by viewModel.selectedMemberId.collectAsState()
    val groupTotalPaid      by viewModel.groupTotalPaid.collectAsState()
    val groupTotalPending   by viewModel.groupTotalPending.collectAsState()
    val membersPaidThisWeek by viewModel.membersPaidThisWeek.collectAsState()
    val weeklyCollection    by viewModel.weeklyCollection.collectAsState()
    val groupSettings       by viewModel.groupSettings.collectAsState()
    val memberTotals        by viewModel.memberTotals.collectAsState()
    val weeklyEntries       by viewModel.weeklyEntries.collectAsState()

    var amountInput     by remember { mutableStateOf("") }
    var status          by remember { mutableStateOf(SavingsStatus.PAID) }
    var showAmountError by remember { mutableStateOf(false) }

    // Bottom sheet visibility
    var showCollectedSheet by remember { mutableStateOf(false) }
    var showPendingSheet   by remember { mutableStateOf(false) }
    var showPaidWeekSheet  by remember { mutableStateOf(false) }
    var showWeeklySheet    by remember { mutableStateOf(false) }

    val selectedMember = members.find { it.id == selectedMemberId }
    val memberNameMap  = remember(members) { members.associate { it.id to it.name } }

    // ── Bottom sheets ─────────────────────────────────────────────────────
    if (showCollectedSheet) {
        GroupCollectedSheet(
            totalCollected = groupTotalPaid,
            memberTotals   = memberTotals,
            onDismiss      = { showCollectedSheet = false }
        )
    }
    if (showPendingSheet) {
        GroupPendingSheet(
            totalPending  = groupTotalPending,
            memberTotals  = memberTotals,
            computeStatus = viewModel::getMemberStatus,
            onDismiss     = { showPendingSheet = false }
        )
    }
    if (showPaidWeekSheet) {
        PaidThisWeekSheet(
            membersPaidCount = membersPaidThisWeek,
            totalMembers     = members.size,
            weeklyEntries    = weeklyEntries,
            memberNameMap    = memberNameMap,
            onDismiss        = { showPaidWeekSheet = false }
        )
    }
    if (showWeeklySheet) {
        WeeklyCollectionSheet(
            weeklyCollection = weeklyCollection,
            weeklyEntries    = weeklyEntries,
            memberNameMap    = memberNameMap,
            onDismiss        = { showWeeklySheet = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ── Interactive group summary mini-cards ──────────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SavingsMiniCard(
                    label     = "Group Collected",
                    value     = "₹${String.format("%.0f", groupTotalPaid)}",
                    color     = StatusPaid,
                    bg        = StatusPaidBg,
                    clickable = true,
                    onClick   = { showCollectedSheet = true },
                    modifier  = Modifier.weight(1f)
                )
                SavingsMiniCard(
                    label     = "Group Pending",
                    value     = "₹${String.format("%.0f", groupTotalPending)}",
                    color     = StatusPending,
                    bg        = StatusPendingBg,
                    clickable = true,
                    onClick   = { showPendingSheet = true },
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SavingsMiniCard(
                    label     = "Paid This Week",
                    value     = "$membersPaidThisWeek members",
                    color     = BrandPrimary,
                    bg        = BrandPrimary.copy(alpha = 0.08f),
                    clickable = true,
                    onClick   = { showPaidWeekSheet = true },
                    modifier  = Modifier.weight(1f)
                )
                SavingsMiniCard(
                    label     = "This Week ₹",
                    value     = "₹${String.format("%.0f", weeklyCollection)}",
                    color     = BrandAccent,
                    bg        = BrandAccent.copy(alpha = 0.10f),
                    clickable = true,
                    onClick   = { showWeeklySheet = true },
                    modifier  = Modifier.weight(1f)
                )
            }
        }

        // ── Select member ─────────────────────────────────────────────────
        item { SectionHeader(title = "Select Member") }

        if (members.isEmpty()) {
            item {
                EmptyState(icon = Icons.Rounded.Group, message = "No members yet.\nAdd members first from the Members tab.")
            }
        } else {
            items(members) { member ->
                val isSelected = member.id == selectedMemberId
                MemberSelectCard(
                    member     = member,
                    isSelected = isSelected,
                    onClick    = {
                        viewModel.selectMember(member.id)
                        amountInput     = ""
                        showAmountError = false
                    }
                )
            }
        }

        // ── Member financial profile ──────────────────────────────────────
        selectedMember?.let { member ->
            item { Spacer(modifier = Modifier.height(2.dp)) }

            item {
                MemberFinancialProfile(
                    member       = member,
                    totalPaid    = totalPaid,
                    totalPending = totalPending,
                    paidCount    = paidCount,
                    pendingCount = pendingCount,
                    lastPaidDate = lastPaidDate,
                    viewModel    = viewModel
                )
            }

            // Add savings entry form
            item {
                PremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MemberAvatar(name = member.name, size = 36.dp)
                            Column {
                                Text("Add Savings Entry", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                                Text(member.name, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        HorizontalDivider(color = DividerColor)

                        groupSettings?.weeklyContribution?.let { weekly ->
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(BrandPrimary.copy(alpha = 0.06f)).padding(10.dp)
                            ) {
                                Text("Weekly contribution: ₹${String.format("%.0f", weekly)}", fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = BrandPrimary, fontWeight = FontWeight.Medium)
                            }
                        }

                        PremiumTextField(
                            value         = amountInput,
                            onValueChange = { amountInput = it; if (showAmountError) showAmountError = it.toDoubleOrNull() == null },
                            label         = "Amount (₹)",
                            isError       = showAmountError,
                            errorMessage  = "Enter a valid amount",
                            keyboardType  = KeyboardType.Decimal,
                            leadingIcon   = { Icon(Icons.Rounded.AttachMoney, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) }
                        )

                        StatusToggle(
                            isPaid            = status == SavingsStatus.PAID,
                            onPaidSelected    = { status = SavingsStatus.PAID },
                            onPendingSelected = { status = SavingsStatus.PENDING }
                        )

                        PrimaryButton(
                            text  = "Save Entry",
                            onClick = {
                                val amount = amountInput.toDoubleOrNull()
                                showAmountError = amount == null || amount <= 0
                                if (amount != null && amount > 0) {
                                    viewModel.addSavings(member.id, amount, status)
                                    amountInput = ""
                                }
                            },
                            modifier    = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Rounded.CheckCircle
                        )
                    }
                }
            }

            if (savingsEntries.isNotEmpty()) {
                item { SectionHeader(title = "Payment History") }
                itemsIndexed(savingsEntries) { index, entry ->
                    SavingsEntryCard(entry = entry, index = index)
                }
            } else {
                item { EmptyState(icon = Icons.Rounded.Savings, message = "No savings entries yet for ${member.name}.") }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Member Financial Profile Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MemberFinancialProfile(
    member: MemberEntity,
    totalPaid: Double,
    totalPending: Double,
    paidCount: Int,
    pendingCount: Int,
    lastPaidDate: Long?,
    viewModel: SavingsViewModel
) {
    val status = viewModel.getMemberStatus(paidCount, pendingCount)
    val (statusColor, statusBg, statusLabel, statusIcon) = when (status) {
        MemberStatus.GOOD_CONTRIBUTOR -> Quad(StatusPaid,    StatusPaidBg,    "Good Contributor", Icons.Rounded.Star)
        MemberStatus.ACTIVE           -> Quad(BrandPrimary,  BrandPrimary.copy(alpha = 0.10f), "Active", Icons.Rounded.CheckCircle)
        MemberStatus.IRREGULAR        -> Quad(BrandAccent,   BrandAccent.copy(alpha = 0.12f),  "Irregular", Icons.Rounded.Warning)
        MemberStatus.PENDING          -> Quad(StatusPending, StatusPendingBg, "Pending", Icons.Rounded.Warning)
    }

    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Financial Profile", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(statusBg).padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(12.dp))
                    Text(statusLabel, color = statusColor, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            }

            HorizontalDivider(color = DividerColor)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileStat("Total Paid",    "₹${String.format("%.0f", totalPaid)}",    StatusPaid,    Modifier.weight(1f))
                ProfileStat("Pending",       "₹${String.format("%.0f", totalPending)}", if (totalPending > 0) StatusPending else TextSecondary, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileStat("Weeks Paid",    "$paidCount",    BrandPrimary,  Modifier.weight(1f))
                ProfileStat("Weeks Missed",  "$pendingCount", if (pendingCount > 0) StatusPending else TextSecondary, Modifier.weight(1f))
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.CalendarMonth, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Text("Last payment: ${DateUtils.formatDateOrNull(lastPaidDate)}", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
            }

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

@Composable
private fun ProfileStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(SurfaceElevated).padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
private fun MemberSelectCard(member: MemberEntity, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) BrandPrimary else DividerColor
    val bgColor     = if (isSelected) BrandPrimary.copy(alpha = 0.05f) else SurfaceCard

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(remember { MutableInteractionSource() }, ripple(bounded = true)) { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MemberAvatar(name = member.name, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                if (member.phone.isNotBlank()) Text(member.phone, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = TextSecondary)
            }
            if (isSelected) Icon(Icons.Rounded.CheckCircle, "Selected", tint = BrandPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SavingsEntryCard(entry: SavingsEntryEntity, index: Int) {
    val chipStatus  = if (entry.status == SavingsStatus.PAID) ChipStatus.PAID else ChipStatus.PENDING
    val amountColor = if (entry.status == SavingsStatus.PAID) StatusPaid else StatusPending

    AnimatedVisibility(visible = true, enter = fadeIn(tween(180, delayMillis = index * 30))) {
        PremiumCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("₹ ${String.format("%.2f", entry.amount)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = amountColor)
                    Text(DateUtils.formatDate(entry.entryDateMillis), fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
                    if (entry.weekNumber > 0) Text("Week ${entry.weekNumber}, ${entry.weekYear}", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
                }
                StatusChip(status = chipStatus)
            }
        }
    }
}

@Composable
private fun SavingsMiniCard(
    label: String,
    value: String,
    color: Color,
    bg: Color,
    clickable: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .then(
                if (clickable) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true)
                ) { onClick() } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = color.copy(alpha = 0.75f))
            Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
            if (clickable) {
                Text("Tap for details →", fontFamily = PoppinsFontFamily, fontSize = 9.sp, color = color.copy(alpha = 0.5f))
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
