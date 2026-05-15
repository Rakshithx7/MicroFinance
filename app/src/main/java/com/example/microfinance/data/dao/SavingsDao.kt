package com.example.microfinance.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.microfinance.data.entity.MemberSavingsTotal
import com.example.microfinance.data.entity.SavingsEntryEntity
import com.example.microfinance.data.entity.SavingsStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {

    @Insert
    suspend fun insert(entry: SavingsEntryEntity): Long

    @Update
    suspend fun update(entry: SavingsEntryEntity)

    @Query("SELECT * FROM savings_entries ORDER BY entryDateMillis DESC")
    suspend fun getAllOnce(): List<SavingsEntryEntity>

    // ── Per-member queries ────────────────────────────────────────────────

    @Query("SELECT * FROM savings_entries WHERE memberId = :memberId ORDER BY entryDateMillis DESC")
    fun getByMember(memberId: Long): Flow<List<SavingsEntryEntity>>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM savings_entries " +
        "WHERE memberId = :memberId AND status = :status"
    )
    fun getTotalForMemberByStatus(memberId: Long, status: SavingsStatus): Flow<Double>

    @Query("SELECT COUNT(*) FROM savings_entries WHERE memberId = :memberId AND status = :status")
    fun getCountForMemberByStatus(memberId: Long, status: SavingsStatus): Flow<Int>

    @Query(
        "SELECT MAX(entryDateMillis) FROM savings_entries " +
        "WHERE memberId = :memberId AND status = 'PAID'"
    )
    fun getLastPaidDateForMember(memberId: Long): Flow<Long?>

    // ── Group-wide queries ────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(amount), 0) FROM savings_entries WHERE status = :status")
    fun getGroupTotalByStatus(status: SavingsStatus): Flow<Double>

    @Query("SELECT COUNT(DISTINCT memberId) FROM savings_entries WHERE status = 'PAID' AND weekNumber = :week AND weekYear = :year")
    fun getMembersPaidInWeek(week: Int, year: Int): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM savings_entries WHERE status = 'PAID' AND weekNumber = :week AND weekYear = :year")
    fun getWeeklyCollection(week: Int, year: Int): Flow<Double>

    // ── Aggregated member totals (for dashboard) ──────────────────────────

    @Query(
        """
        SELECT
            m.id   AS memberId,
            m.name AS memberName,
            COALESCE(SUM(CASE WHEN s.status = 'PAID'    THEN s.amount ELSE 0 END), 0) AS totalPaid,
            COALESCE(SUM(CASE WHEN s.status = 'PENDING' THEN s.amount ELSE 0 END), 0) AS totalPending,
            COALESCE(SUM(CASE WHEN s.status = 'PAID'    THEN 1        ELSE 0 END), 0) AS paidCount,
            COALESCE(SUM(CASE WHEN s.status = 'PENDING' THEN 1        ELSE 0 END), 0) AS pendingCount
        FROM members m
        LEFT JOIN savings_entries s ON m.id = s.memberId
        GROUP BY m.id
        ORDER BY totalPaid DESC
        """
    )
    fun getMemberTotals(): Flow<List<MemberSavingsTotal>>

    // ── Weekly check: did this member pay in a given week? ────────────────

    @Query(
        "SELECT COUNT(*) FROM savings_entries " +
        "WHERE memberId = :memberId AND weekNumber = :week AND weekYear = :year AND status = 'PAID'"
    )
    suspend fun didMemberPayInWeek(memberId: Long, week: Int, year: Int): Int

    /** All entries for a given week (for drill-down bottom sheet) */
    @Query(
        "SELECT * FROM savings_entries WHERE weekNumber = :week AND weekYear = :year ORDER BY entryDateMillis DESC"
    )
    fun getEntriesForWeek(week: Int, year: Int): Flow<List<SavingsEntryEntity>>
}
