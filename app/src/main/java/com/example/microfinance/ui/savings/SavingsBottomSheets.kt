package com.example.microfinance.ui.savings

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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.microfinance.data.entity.MemberSavingsTotal
import com.example.microfinance.data.entity.MemberStatus
import com.example.microfinance.data.entity.SavingsEntryEntity
import com.example.microfinance.data.entity.SavingsStatus
import com.example.microfinance.ui.components.AnalyticsBottomSheet
import com.example.microfinance.ui.components.ChipStatus
import com.example.microfinance.ui.components.MemberAvatar
import com.example.microfinance.ui.components.StatusChip
import com.example.microfinance.ui.theme.BrandAccent
import com.example.microfinance.ui.theme.BrandPrimary
import com.example.microfinance.ui.theme.DividerColor
import com.example.microfinance.ui.theme.PoppinsFontFamily
import com.example.microfinance.ui.theme.StatusPaid
import com.example.microfinance.ui.theme.StatusPaidBg
import com.example.microfinance.ui.theme.StatusPending
import com.example.microfinance.ui.theme.StatusPendingBg
import com.example.microfinance.ui.theme.SurfaceElevated
import com.example.microfinance.ui.theme.TextPrimary
import com.example.microfinance.ui.theme.TextSecondary
import com.example.microfinance.util.DateUtils

// ─────────────────────────────────────────────────────────────────────────────
// 1. Group Collected — member-wise contribution breakdown
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCollectedSheet(
    totalCollected: Double,
    memberTotals: List<MemberSavingsTotal>,
    onDismiss: () -> Unit
) {
    AnalyticsBottomSheet(
        title    = "Group Collected",
        subtitle = "₹${String.format("%.2f", totalCollected)} total savings",
        icon     = Icons.Rounded.Savings,
        iconTint = StatusPaid,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SheetStat("Total Collected", "₹${String.format("%.0f", totalCollected)}", StatusPaid, StatusPaidBg, Modifier.weight(1f))
                    SheetStat("Contributors", "${memberTotals.count { it.totalPaid > 0 }}", BrandPrimary, BrandPrimary.copy(alpha = 0.08f), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (memberTotals.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No savings data yet", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                items(memberTotals.sortedByDescending { it.totalPaid }) { m ->
                    MemberContributionRow(m = m, totalCollected = totalCollected)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun MemberContributionRow(m: MemberSavingsTotal, totalCollected: Double) {
    val share = if (totalCollected > 0) (m.totalPaid / totalCollected).toFloat() else 0f
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MemberAvatar(name = m.memberName, size = 38.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(m.memberName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Text("${m.paidCount} payments", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            // Progress bar
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50.dp)).background(SurfaceElevated)) {
                Box(modifier = Modifier.fillMaxWidth(share).height(4.dp).clip(RoundedCornerShape(50.dp)).background(StatusPaid))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("₹${String.format("%.0f", m.totalPaid)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusPaid)
            Text("${(share * 100).toInt()}%", fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = TextSecondary)
        }
    }
    HorizontalDivider(color = DividerColor)
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. Group Pending — member-wise pending breakdown with risk badges
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPendingSheet(
    totalPending: Double,
    memberTotals: List<MemberSavingsTotal>,
    computeStatus: (Int, Int) -> MemberStatus,
    onDismiss: () -> Unit
) {
    val pendingMembers = memberTotals.filter { it.totalPending > 0 }.sortedByDescending { it.totalPending }

    AnalyticsBottomSheet(
        title    = "Pending Dues",
        subtitle = "₹${String.format("%.2f", totalPending)} outstanding",
        icon     = Icons.Rounded.TrendingDown,
        iconTint = StatusPending,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetStat("Total Pending", "₹${String.format("%.0f", totalPending)}", StatusPending, StatusPendingBg, Modifier.weight(1f))
                    SheetStat("Members Due", "${pendingMembers.size}", BrandAccent, BrandAccent.copy(alpha = 0.10f), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (pendingMembers.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("🎉 No pending dues!", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = StatusPaid)
                    }
                }
            } else {
                items(pendingMembers) { m ->
                    val status = computeStatus(m.paidCount, m.pendingCount)
                    PendingMemberRow(m = m, status = status)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun PendingMemberRow(m: MemberSavingsTotal, status: MemberStatus) {
    val (statusColor, statusBg, statusLabel) = when (status) {
        MemberStatus.GOOD_CONTRIBUTOR -> Triple(StatusPaid,    StatusPaidBg,    "Good")
        MemberStatus.ACTIVE           -> Triple(BrandPrimary,  BrandPrimary.copy(alpha = 0.10f), "Active")
        MemberStatus.IRREGULAR        -> Triple(BrandAccent,   BrandAccent.copy(alpha = 0.12f),  "Irregular")
        MemberStatus.PENDING          -> Triple(StatusPending, StatusPendingBg, "High Risk")
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MemberAvatar(name = m.memberName, size = 38.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(m.memberName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(statusLabel, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = statusColor)
            }
            Text("${m.pendingCount} missed payments", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
        }
        Text("₹${String.format("%.0f", m.totalPending)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusPending)
    }
    HorizontalDivider(color = DividerColor)
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. Paid This Week — who paid this week
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaidThisWeekSheet(
    membersPaidCount: Int,
    totalMembers: Int,
    weeklyEntries: List<SavingsEntryEntity>,
    memberNameMap: Map<Long, String>,
    onDismiss: () -> Unit
) {
    AnalyticsBottomSheet(
        title    = "Paid This Week",
        subtitle = "$membersPaidCount of $totalMembers members paid",
        icon     = Icons.Rounded.Group,
        iconTint = BrandPrimary,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetStat("Paid", "$membersPaidCount", StatusPaid, StatusPaidBg, Modifier.weight(1f))
                    SheetStat("Pending", "${totalMembers - membersPaidCount}", StatusPending, StatusPendingBg, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            val paidEntries = weeklyEntries.filter { it.status == SavingsStatus.PAID }
            if (paidEntries.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No payments this week yet", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                items(paidEntries) { entry ->
                    val name = memberNameMap[entry.memberId] ?: "Member"
                    WeeklyEntryRow(entry = entry, memberName = name)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun WeeklyEntryRow(entry: SavingsEntryEntity, memberName: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MemberAvatar(name = memberName, size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(memberName, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.CalendarMonth, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Text(DateUtils.formatDate(entry.entryDateMillis), fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = TextSecondary)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("₹${String.format("%.0f", entry.amount)}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusPaid)
            StatusChip(status = ChipStatus.PAID)
        }
    }
    HorizontalDivider(color = DividerColor)
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. This Week Collection — weekly breakdown
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyCollectionSheet(
    weeklyCollection: Double,
    weeklyEntries: List<SavingsEntryEntity>,
    memberNameMap: Map<Long, String>,
    onDismiss: () -> Unit
) {
    val paidEntries    = weeklyEntries.filter { it.status == SavingsStatus.PAID }
    val pendingEntries = weeklyEntries.filter { it.status == SavingsStatus.PENDING }

    AnalyticsBottomSheet(
        title    = "This Week Collection",
        subtitle = "₹${String.format("%.2f", weeklyCollection)} collected",
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
                    SheetStat("Collected", "₹${String.format("%.0f", weeklyCollection)}", StatusPaid, StatusPaidBg, Modifier.weight(1f))
                    SheetStat("Transactions", "${paidEntries.size}", BrandPrimary, BrandPrimary.copy(alpha = 0.08f), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (weeklyEntries.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions this week", fontFamily = PoppinsFontFamily, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            } else {
                items(weeklyEntries) { entry ->
                    val name = memberNameMap[entry.memberId] ?: "Member"
                    WeeklyEntryRow(entry = entry, memberName = name)
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared helper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SheetStat(label: String, value: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(bg).padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, fontFamily = PoppinsFontFamily, fontSize = 10.sp, color = color.copy(alpha = 0.75f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
    }
}
