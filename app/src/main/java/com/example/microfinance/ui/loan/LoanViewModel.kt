package com.example.microfinance.ui.loan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.microfinance.data.db.DatabaseProvider
import com.example.microfinance.data.entity.LoanEntity
import com.example.microfinance.data.entity.LoanWithRepaymentTotal
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.data.entity.RepaymentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class LoanViewModel(application: Application) : AndroidViewModel(application) {

    private val db           = DatabaseProvider.getDatabase(application)
    private val memberDao    = db.memberDao()
    private val loanDao      = db.loanDao()
    private val repaymentDao = db.repaymentDao()

    val members: StateFlow<List<MemberEntity>> = memberDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedMemberId = MutableStateFlow<Long?>(null)
    private val selectedLoanId   = MutableStateFlow<Long?>(null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // ── Loans for selected member ─────────────────────────────────────────

    val loansForSelectedMember: StateFlow<List<LoanEntity>> =
        selectedMemberId.flatMapLatest { memberId ->
            if (memberId == null) flowOf(emptyList())
            else loanDao.getLoansForMember(memberId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── ✅ FIX: selectedLoan is now REACTIVE — driven from Room, not local state ──
    // When addRepayment() closes the loan (isClosed = true), Room emits the
    // updated LoanEntity here automatically. The UI always sees fresh data.

    val selectedLoan: StateFlow<LoanEntity?> =
        selectedLoanId.flatMapLatest { loanId ->
            if (loanId == null) flowOf(null)
            else loanDao.getByIdFlow(loanId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Repayments for selected loan ──────────────────────────────────────

    val repaymentsForSelectedLoan: StateFlow<List<RepaymentEntity>> =
        selectedLoanId.flatMapLatest { loanId ->
            if (loanId == null) flowOf(emptyList())
            else repaymentDao.getByLoan(loanId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── ✅ FIX: totalRepaid is reactive — updates instantly on repayment insert ──

    val totalRepaidForSelectedLoan: StateFlow<Double> =
        selectedLoanId.flatMapLatest { loanId ->
            if (loanId == null) flowOf(0.0)
            else repaymentDao.getTotalRepaidForLoan(loanId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── ✅ FIX: outstanding is derived from BOTH reactive flows ──────────────
    // When either selectedLoan OR totalRepaid changes, outstanding recomputes.

    val outstandingForSelectedLoan: StateFlow<Double> = combine(
        selectedLoan,
        totalRepaidForSelectedLoan
    ) { loan, repaid ->
        if (loan == null) 0.0
        else calculateOutstanding(loan, repaid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalDueForSelectedLoan: StateFlow<Double> =
        selectedLoan.map { loan ->
            if (loan == null) 0.0 else calculateTotalDue(loan)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Loan analytics ────────────────────────────────────────────────────

    val openLoansCount: StateFlow<Int> = loanDao.getOpenLoansCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val closedLoansCount: StateFlow<Int> = loanDao.getClosedLoansCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalActiveLoanAmount: StateFlow<Double> = loanDao.getTotalActiveLoanAmount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** ✅ True outstanding balance (principal + interest − repaid) for all open loans */
    val totalOutstandingBalance: StateFlow<Double> = loanDao.getTotalOutstandingBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalInterestEarned: StateFlow<Double> = loanDao.getTotalInterestEarned()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ✅ FIX: overdue count uses a live ticker — refreshes every minute
    val overdueLoansCount: StateFlow<Int> =
        liveNowFlow()
            .flatMapLatest { now -> loanDao.getOverdueLoansCount(now) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Drill-down: all loans with repayment totals ───────────────────────

    val allLoansWithTotals: StateFlow<List<LoanWithRepaymentTotal>> =
        loanDao.getAllLoansWithRepaymentTotals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val loansWithTotalsForSelectedMember: StateFlow<List<LoanWithRepaymentTotal>> =
        selectedMemberId.flatMapLatest { memberId ->
            if (memberId == null) flowOf(emptyList())
            else loanDao.getLoansWithRepaymentTotalsForMember(memberId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeLoansWithTotals: StateFlow<List<LoanWithRepaymentTotal>> =
        allLoansWithTotals
            .map { list -> list.filter { !it.isClosed } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val closedLoansWithTotals: StateFlow<List<LoanWithRepaymentTotal>> =
        allLoansWithTotals
            .map { list -> list.filter { it.isClosed } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val overdueLoansWithTotals: StateFlow<List<LoanWithRepaymentTotal>> =
        allLoansWithTotals
            .map { list -> list.filter { it.isOverdue } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Selection ─────────────────────────────────────────────────────────

    fun selectMember(memberId: Long) {
        selectedMemberId.value = memberId
        selectedLoanId.value   = null
        _message.value         = null
    }

    fun selectLoan(loanId: Long?) {
        selectedLoanId.value = loanId
        _message.value       = null
    }

    fun clearMessage() {
        _message.value = null
    }

    // ── Calculations (time-based simple interest) ─────────────────────────

    fun calculateInterest(loan: LoanEntity): Double =
        loan.principalAmount * loan.interestRatePercent * maxOf(loan.durationMonths, 1) / 100.0

    fun calculateTotalDue(loan: LoanEntity): Double =
        loan.principalAmount + calculateInterest(loan)

    fun calculateOutstanding(loan: LoanEntity, totalRepaid: Double): Double =
        (calculateTotalDue(loan) - totalRepaid).coerceAtLeast(0.0)

    // ── Actions ───────────────────────────────────────────────────────────

    fun createLoan(
        memberId: Long,
        principal: Double,
        interestRate: Double,
        durationMonths: Int = 0
    ) {
        if (principal <= 0 || interestRate < 0) {
            _message.value = "Enter valid loan values"
            return
        }

        viewModelScope.launch {
            val activeLoan = loanDao.getActiveLoanForMember(memberId)
            if (activeLoan != null) {
                _message.value = "Member already has an active loan"
                return@launch
            }

            val now = System.currentTimeMillis()
            val dueDate = if (durationMonths > 0) {
                Calendar.getInstance().apply {
                    timeInMillis = now
                    add(Calendar.MONTH, durationMonths)
                }.timeInMillis
            } else 0L

            val loanId = loanDao.insert(
                LoanEntity(
                    memberId            = memberId,
                    principalAmount     = principal,
                    interestRatePercent = interestRate,
                    startDateMillis     = now,
                    durationMonths      = durationMonths,
                    dueDateMillis       = dueDate,
                    isClosed            = false
                )
            )
            selectedLoanId.value = loanId
            val interest = principal * interestRate * maxOf(durationMonths, 1) / 100.0
            val totalDue = principal + interest
            _message.value = "Loan ₹${fmt(principal)} issued. Total due: ₹${fmt(totalDue)}"
        }
    }

    fun addRepayment(loanId: Long, amount: Double) {
        if (amount <= 0) {
            _message.value = "Enter a valid repayment amount"
            return
        }

        viewModelScope.launch {
            // 1. Insert repayment — Room emits to all observing Flows immediately
            repaymentDao.insert(
                RepaymentEntity(
                    loanId       = loanId,
                    amount       = amount,
                    paidAtMillis = System.currentTimeMillis()
                )
            )

            // 2. Check if loan should be auto-closed
            val loan = loanDao.getById(loanId)
            if (loan != null && !loan.isClosed) {
                val totalRepaid = repaymentDao.getTotalRepaidForLoanOnce(loanId)
                val totalDue    = calculateTotalDue(loan)
                if (totalRepaid >= totalDue) {
                    // 3. Mark closed — Room emits updated LoanEntity to selectedLoan Flow
                    loanDao.update(loan.copy(isClosed = true))
                    _message.value = "🎉 Loan fully repaid and closed!"
                } else {
                    val remaining = (totalDue - totalRepaid).coerceAtLeast(0.0)
                    _message.value = "Repayment added. Remaining: ₹${fmt(remaining)}"
                }
            }
            // All StateFlows (totalRepaid, outstanding, allLoansWithTotals,
            // openLoansCount, dashboard) update automatically via Room's
            // reactive query invalidation — no manual refresh needed.
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun fmt(v: Double) = String.format("%.2f", v)

    /**
     * Emits the current timestamp every 60 seconds.
     * Used to keep overdue detection fresh without a stale captured value.
     */
    private fun liveNowFlow() = flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(TimeUnit.MINUTES.toMillis(1))
        }
    }
}
