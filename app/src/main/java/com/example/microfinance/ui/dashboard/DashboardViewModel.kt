package com.example.microfinance.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.microfinance.data.db.DatabaseProvider
import com.example.microfinance.data.entity.MemberSavingsTotal
import com.example.microfinance.data.entity.MemberStatus
import com.example.microfinance.data.entity.SavingsStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db               = DatabaseProvider.getDatabase(application)
    private val memberDao        = db.memberDao()
    private val savingsDao       = db.savingsDao()
    private val loanDao          = db.loanDao()
    private val repaymentDao     = db.repaymentDao()
    private val groupSettingsDao = db.groupSettingsDao()

    private val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // ── Savings ───────────────────────────────────────────────────────────

    val totalPaid: StateFlow<Double> = savingsDao.getGroupTotalByStatus(SavingsStatus.PAID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalPending: StateFlow<Double> = savingsDao.getGroupTotalByStatus(SavingsStatus.PENDING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val memberTotals: StateFlow<List<MemberSavingsTotal>> = savingsDao.getMemberTotals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val membersPaidThisWeek: StateFlow<Int> =
        savingsDao.getMembersPaidInWeek(currentWeek, currentYear)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val weeklyCollection: StateFlow<Double> =
        savingsDao.getWeeklyCollection(currentWeek, currentYear)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Members ───────────────────────────────────────────────────────────

    val totalMembersCount: StateFlow<Int> = memberDao.getAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Loans ─────────────────────────────────────────────────────────────

    val openLoansCount: StateFlow<Int> = loanDao.getOpenLoansCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val closedLoansCount: StateFlow<Int> = loanDao.getClosedLoansCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalActiveLoanAmount: StateFlow<Double> = loanDao.getTotalActiveLoanAmount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalLoanAmountIssued: StateFlow<Double> = loanDao.getTotalLoanAmountIssued()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** True outstanding balance (principal + interest − repaid) for all open loans */
    val totalOutstandingBalance: StateFlow<Double> = loanDao.getTotalOutstandingBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** Time-based SI interest earned from closed loans */
    val totalInterestEarned: StateFlow<Double> = loanDao.getTotalInterestEarned()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ✅ FIX: live ticker — overdue detection refreshes every minute
    val overdueLoansCount: StateFlow<Int> =
        liveNowFlow()
            .flatMapLatest { now -> loanDao.getOverdueLoansCount(now) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Repayments ────────────────────────────────────────────────────────

    val totalGroupRepaid: StateFlow<Double> = repaymentDao.getGroupTotalRepaid()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Derived: Group Capital (CORRECTED formula) ────────────────────────
    //
    // Available Cash = Total Savings Collected − Total Loans Disbursed + Total Repaid
    //
    // Explanation:
    //   - Savings collected = cash that came IN from members
    //   - Loans disbursed   = cash that went OUT to borrowers
    //   - Repayments        = cash that came BACK from borrowers
    //   - Interest earned   = additional income from closed loans
    //
    // Group Capital = Savings + Interest Earned − Active Loan Exposure
    // (Active loans are assets but not liquid cash — they're out in the field)

    val groupCapital: StateFlow<Double> = combine(
        totalPaid,
        totalInterestEarned,
        totalLoanAmountIssued,
        totalGroupRepaid
    ) { savings, interest, issued, repaid ->
        // Cash in hand = savings collected + repayments received − loans disbursed
        (savings + repaid + interest - issued).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Derived: Recovery Rate ────────────────────────────────────────────

    val recoveryRate: StateFlow<Double> = combine(
        totalGroupRepaid,
        totalLoanAmountIssued
    ) { repaid, issued ->
        if (issued <= 0) 0.0 else (repaid / issued * 100.0).coerceIn(0.0, 100.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Derived: Members pending this week ───────────────────────────────

    val membersPendingThisWeek: StateFlow<Int> = combine(
        totalMembersCount,
        membersPaidThisWeek
    ) { total, paid -> (total - paid).coerceAtLeast(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Group settings ────────────────────────────────────────────────────

    val groupSettings = groupSettingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Alerts ────────────────────────────────────────────────────────────

    val membersWithPending: StateFlow<List<MemberSavingsTotal>> = memberTotals
        .map { list -> list.filter { it.totalPending > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val highRiskMembers: StateFlow<List<MemberSavingsTotal>> = memberTotals
        .map { list ->
            list.filter { m ->
                val status = computeMemberStatus(m.paidCount, m.pendingCount)
                status == MemberStatus.IRREGULAR || status == MemberStatus.PENDING
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Helpers ───────────────────────────────────────────────────────────

    fun computeMemberStatus(paidCount: Int, pendingCount: Int): MemberStatus {
        val total = paidCount + pendingCount
        if (total == 0) return MemberStatus.ACTIVE
        val consistency = paidCount.toDouble() / total
        return when {
            consistency >= 0.90 -> MemberStatus.GOOD_CONTRIBUTOR
            consistency >= 0.70 -> MemberStatus.ACTIVE
            consistency >= 0.40 -> MemberStatus.IRREGULAR
            else                -> MemberStatus.PENDING
        }
    }

    // ── WhatsApp export ───────────────────────────────────────────────────

    suspend fun buildSummaryText(): String {
        val members     = memberDao.getAll().first()
        val paid        = totalPaid.value
        val pending     = totalPending.value
        val openLoans   = openLoansCount.value
        val closedLoans = closedLoansCount.value
        val activeAmt   = totalActiveLoanAmount.value
        val interest    = totalInterestEarned.value
        val capital     = groupCapital.value
        val recovery    = recoveryRate.value
        val perMember   = memberTotals.value
        val settings    = groupSettingsDao.getSettingsOnce()

        return buildString {
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("  ${settings?.groupName ?: "Mahila-Shakti Unnati"}")
            appendLine("  Micro Finance — Group Summary")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("👥 Total Members   : ${members.size}")
            appendLine("💰 Total Savings   : ₹${fmt(paid)}")
            appendLine("⏳ Total Pending   : ₹${fmt(pending)}")
            appendLine("🏦 Group Capital   : ₹${fmt(capital)}")
            appendLine("📈 Interest Earned : ₹${fmt(interest)}")
            appendLine()
            appendLine("📋 Loans")
            appendLine("  Active  : $openLoans  (₹${fmt(activeAmt)})")
            appendLine("  Closed  : $closedLoans")
            appendLine("  Recovery: ${String.format("%.1f", recovery)}%")
            appendLine()
            appendLine("📊 Member Savings:")
            if (perMember.isEmpty()) {
                appendLine("  No savings data yet")
            } else {
                perMember.forEach { m ->
                    appendLine("  • ${m.memberName}")
                    appendLine("    Paid: ₹${fmt(m.totalPaid)}  |  Pending: ₹${fmt(m.totalPending)}")
                }
            }
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    private fun fmt(v: Double) = String.format("%.2f", v)

    /** Emits current timestamp every 60 seconds for live overdue detection */
    private fun liveNowFlow() = flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(TimeUnit.MINUTES.toMillis(1))
        }
    }
}
