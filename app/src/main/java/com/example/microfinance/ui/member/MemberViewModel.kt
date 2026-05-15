package com.example.microfinance.ui.member

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.microfinance.data.db.DatabaseProvider
import com.example.microfinance.data.entity.LoanWithRepaymentTotal
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.data.entity.MemberSavingsTotal
import com.example.microfinance.data.entity.MemberStatus
import com.example.microfinance.data.entity.SavingsEntryEntity
import com.example.microfinance.data.entity.SavingsStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemberViewModel(application: Application) : AndroidViewModel(application) {

    private val db           = DatabaseProvider.getDatabase(application)
    private val memberDao    = db.memberDao()
    private val savingsDao   = db.savingsDao()
    private val loanDao      = db.loanDao()
    private val repaymentDao = db.repaymentDao()

    val members: StateFlow<List<MemberEntity>> = memberDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Delete state ──────────────────────────────────────────────────────

    private val _deleteMessage = MutableStateFlow<String?>(null)
    val deleteMessage: StateFlow<String?> = _deleteMessage.asStateFlow()

    // ── Detail: selected member for profile screen ────────────────────────

    private val _detailMemberId = MutableStateFlow<Long?>(null)
    val detailMemberId: StateFlow<Long?> = _detailMemberId.asStateFlow()

    val detailMember: StateFlow<MemberEntity?> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(null) else memberDao.getById(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val detailSavingsEntries: StateFlow<List<SavingsEntryEntity>> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else savingsDao.getByMember(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val detailTotalPaid: StateFlow<Double> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0.0)
            else savingsDao.getTotalForMemberByStatus(id, SavingsStatus.PAID)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val detailTotalPending: StateFlow<Double> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0.0)
            else savingsDao.getTotalForMemberByStatus(id, SavingsStatus.PENDING)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val detailPaidCount: StateFlow<Int> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0)
            else savingsDao.getCountForMemberByStatus(id, SavingsStatus.PAID)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val detailPendingCount: StateFlow<Int> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0)
            else savingsDao.getCountForMemberByStatus(id, SavingsStatus.PENDING)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val detailLastPaidDate: StateFlow<Long?> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(null)
            else savingsDao.getLastPaidDateForMember(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val detailLoansWithTotals: StateFlow<List<LoanWithRepaymentTotal>> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else loanDao.getLoansWithRepaymentTotalsForMember(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val detailTotalRepaid: StateFlow<Double> =
        _detailMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0.0)
            else repaymentDao.getTotalRepaidForMember(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Actions ───────────────────────────────────────────────────────────

    fun addMember(name: String, phone: String, photoUri: String?) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            memberDao.insert(
                MemberEntity(
                    name             = cleanName,
                    phone            = phone.trim(),
                    photoUri         = photoUri?.trim().takeUnless { it.isNullOrEmpty() },
                    createdAtMillis  = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Safe delete: checks for active loans first.
     * If active loan exists → sets deleteMessage with warning.
     * Otherwise → deletes (Room CASCADE removes savings + loans + repayments).
     */
    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            val activeLoans = memberDao.getActiveLoanCountForMember(member.id)
            if (activeLoans > 0) {
                _deleteMessage.value =
                    "Cannot delete ${member.name} — they have $activeLoans active loan(s). Close the loan first."
            } else {
                memberDao.delete(member)
                _deleteMessage.value = "${member.name} has been removed."
                // If we were viewing this member's detail, close it
                if (_detailMemberId.value == member.id) _detailMemberId.value = null
            }
        }
    }

    fun clearDeleteMessage() {
        _deleteMessage.value = null
    }

    fun openDetail(memberId: Long) {
        _detailMemberId.value = memberId
    }

    fun closeDetail() {
        _detailMemberId.value = null
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    fun getMemberStatus(paidCount: Int, pendingCount: Int): MemberStatus {
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
}
