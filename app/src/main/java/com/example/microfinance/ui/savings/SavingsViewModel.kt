package com.example.microfinance.ui.savings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.microfinance.data.db.DatabaseProvider
import com.example.microfinance.data.entity.GroupSettingsEntity
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.data.entity.MemberSavingsTotal
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
import java.util.Calendar

class SavingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db               = DatabaseProvider.getDatabase(application)
    private val memberDao        = db.memberDao()
    private val savingsDao       = db.savingsDao()
    private val groupSettingsDao = db.groupSettingsDao()

    // ── Members ───────────────────────────────────────────────────────────

    val members: StateFlow<List<MemberEntity>> = memberDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Group settings ────────────────────────────────────────────────────

    val groupSettings: StateFlow<GroupSettingsEntity?> = groupSettingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Selected member ───────────────────────────────────────────────────

    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    val selectedMemberId: StateFlow<Long?> = _selectedMemberId.asStateFlow()

    val savingsForSelectedMember: StateFlow<List<SavingsEntryEntity>> =
        _selectedMemberId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else savingsDao.getByMember(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalPaidForSelected: StateFlow<Double> =
        _selectedMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0.0)
            else savingsDao.getTotalForMemberByStatus(id, SavingsStatus.PAID)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalPendingForSelected: StateFlow<Double> =
        _selectedMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0.0)
            else savingsDao.getTotalForMemberByStatus(id, SavingsStatus.PENDING)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val paidCountForSelected: StateFlow<Int> =
        _selectedMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0)
            else savingsDao.getCountForMemberByStatus(id, SavingsStatus.PAID)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val pendingCountForSelected: StateFlow<Int> =
        _selectedMemberId.flatMapLatest { id ->
            if (id == null) flowOf(0)
            else savingsDao.getCountForMemberByStatus(id, SavingsStatus.PENDING)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val lastPaidDateForSelected: StateFlow<Long?> =
        _selectedMemberId.flatMapLatest { id ->
            if (id == null) flowOf(null)
            else savingsDao.getLastPaidDateForMember(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Group-wide aggregates ─────────────────────────────────────────────

    val groupTotalPaid: StateFlow<Double> = savingsDao.getGroupTotalByStatus(SavingsStatus.PAID)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val groupTotalPending: StateFlow<Double> = savingsDao.getGroupTotalByStatus(SavingsStatus.PENDING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val memberTotals: StateFlow<List<MemberSavingsTotal>> = savingsDao.getMemberTotals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── This-week stats ───────────────────────────────────────────────────

    private val currentWeek = currentIsoWeek()
    private val currentYear = currentIsoYear()

    val membersPaidThisWeek: StateFlow<Int> =
        savingsDao.getMembersPaidInWeek(currentWeek, currentYear)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val weeklyCollection: StateFlow<Double> =
        savingsDao.getWeeklyCollection(currentWeek, currentYear)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    /** All savings entries for the current week — used by drill-down sheets */
    val weeklyEntries: StateFlow<List<SavingsEntryEntity>> =
        savingsDao.getEntriesForWeek(currentWeek, currentYear)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Actions ───────────────────────────────────────────────────────────

    fun selectMember(memberId: Long) {
        _selectedMemberId.value = memberId
    }

    fun addSavings(memberId: Long, amount: Double, status: SavingsStatus) {
        if (amount <= 0) return
        val cal  = Calendar.getInstance()
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        val year = cal.get(Calendar.YEAR)

        viewModelScope.launch {
            savingsDao.insert(
                SavingsEntryEntity(
                    memberId       = memberId,
                    amount         = amount,
                    status         = status,
                    entryDateMillis = System.currentTimeMillis(),
                    weekNumber     = week,
                    weekYear       = year
                )
            )
        }
    }

    fun updateGroupSettings(weeklyContribution: Double) {
        viewModelScope.launch {
            val existing = groupSettingsDao.getSettingsOnce()
                ?: GroupSettingsEntity()
            groupSettingsDao.upsert(
                existing.copy(weeklyContribution = weeklyContribution)
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    fun getMemberStatus(paidCount: Int, pendingCount: Int): com.example.microfinance.data.entity.MemberStatus {
        val total = paidCount + pendingCount
        if (total == 0) return com.example.microfinance.data.entity.MemberStatus.ACTIVE
        val consistency = paidCount.toDouble() / total
        return when {
            consistency >= 0.90 -> com.example.microfinance.data.entity.MemberStatus.GOOD_CONTRIBUTOR
            consistency >= 0.70 -> com.example.microfinance.data.entity.MemberStatus.ACTIVE
            consistency >= 0.40 -> com.example.microfinance.data.entity.MemberStatus.IRREGULAR
            else                -> com.example.microfinance.data.entity.MemberStatus.PENDING
        }
    }

    private fun currentIsoWeek(): Int =
        Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

    private fun currentIsoYear(): Int =
        Calendar.getInstance().get(Calendar.YEAR)
}
