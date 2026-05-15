package com.example.microfinance.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.microfinance.data.entity.LoanEntity
import com.example.microfinance.data.entity.LoanWithRepaymentTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Insert
    suspend fun insert(loan: LoanEntity): Long

    @Update
    suspend fun update(loan: LoanEntity)

    @Query("SELECT * FROM loans ORDER BY startDateMillis DESC")
    suspend fun getAllOnce(): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE memberId = :memberId ORDER BY startDateMillis DESC")
    fun getLoansForMember(memberId: Long): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE memberId = :memberId AND isClosed = 0 LIMIT 1")
    suspend fun getActiveLoanForMember(memberId: Long): LoanEntity?

    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    suspend fun getById(loanId: Long): LoanEntity?

    /** Reactive version — emits whenever the loan row changes (e.g., isClosed update) */
    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    fun getByIdFlow(loanId: Long): Flow<LoanEntity?>

    // ── Counts ────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM loans WHERE isClosed = 0")
    fun getOpenLoansCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans WHERE isClosed = 1")
    fun getClosedLoansCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM loans")
    fun getTotalLoansCount(): Flow<Int>

    // ── Aggregations ──────────────────────────────────────────────────────

    /** Sum of principal for all open (active) loans */
    @Query("SELECT COALESCE(SUM(principalAmount), 0) FROM loans WHERE isClosed = 0")
    fun getTotalActiveLoanAmount(): Flow<Double>

    /**
     * True outstanding balance across all open loans =
     * SUM(totalDue - totalRepaid) for each open loan.
     * Uses time-based SI: totalDue = P + P*R*MAX(T,1)/100
     */
    @Query(
        """
        SELECT COALESCE(
            SUM(
                (l.principalAmount + l.principalAmount * l.interestRatePercent * MAX(l.durationMonths, 1) / 100.0)
                - COALESCE(r.repaid, 0)
            ), 0
        )
        FROM loans l
        LEFT JOIN (
            SELECT loanId, SUM(amount) AS repaid FROM repayments GROUP BY loanId
        ) r ON r.loanId = l.id
        WHERE l.isClosed = 0
        """
    )
    fun getTotalOutstandingBalance(): Flow<Double>

    /** Sum of principal for all loans ever issued */
    @Query("SELECT COALESCE(SUM(principalAmount), 0) FROM loans")
    fun getTotalLoanAmountIssued(): Flow<Double>

    /**
     * Total interest earned from CLOSED loans using time-based simple interest.
     * SI = P × R × T / 100  where T = durationMonths (or 1 if open-ended).
     * For open-ended loans (durationMonths = 0) we use 1 month as minimum.
     */
    @Query(
        """
        SELECT COALESCE(
            SUM(principalAmount * interestRatePercent * MAX(durationMonths, 1) / 100.0),
            0
        )
        FROM loans WHERE isClosed = 1
        """
    )
    fun getTotalInterestEarned(): Flow<Double>

    /**
     * Projected interest on ALL loans (open + closed) — used for total interest exposure.
     */
    @Query(
        """
        SELECT COALESCE(
            SUM(principalAmount * interestRatePercent * MAX(durationMonths, 1) / 100.0),
            0
        )
        FROM loans
        """
    )
    fun getTotalProjectedInterest(): Flow<Double>

    /** Overdue loans: open loans whose dueDateMillis > 0 and < now */
    @Query(
        "SELECT * FROM loans WHERE isClosed = 0 AND dueDateMillis > 0 AND dueDateMillis < :nowMillis"
    )
    fun getOverdueLoans(nowMillis: Long): Flow<List<LoanEntity>>

    @Query(
        "SELECT COUNT(*) FROM loans WHERE isClosed = 0 AND dueDateMillis > 0 AND dueDateMillis < :nowMillis"
    )
    fun getOverdueLoansCount(nowMillis: Long): Flow<Int>

    /** All open loans (for dashboard exposure calculation) */
    @Query("SELECT * FROM loans WHERE isClosed = 0")
    fun getAllOpenLoans(): Flow<List<LoanEntity>>

    /**
     * Per-member loan summary: each loan joined with its total repaid.
     * Used for Member Detail Screen and Loan Exposure drill-down.
     */
    @Query(
        """
        SELECT
            l.id              AS loanId,
            l.memberId        AS memberId,
            l.principalAmount AS principalAmount,
            l.interestRatePercent AS interestRatePercent,
            l.durationMonths  AS durationMonths,
            l.startDateMillis AS startDateMillis,
            l.dueDateMillis   AS dueDateMillis,
            l.isClosed        AS isClosed,
            COALESCE(SUM(r.amount), 0) AS totalRepaid
        FROM loans l
        LEFT JOIN repayments r ON r.loanId = l.id
        WHERE l.memberId = :memberId
        GROUP BY l.id
        ORDER BY l.startDateMillis DESC
        """
    )
    fun getLoansWithRepaymentTotalsForMember(memberId: Long): Flow<List<LoanWithRepaymentTotal>>

    /**
     * All loans with repayment totals — for Loan Exposure drill-down.
     */
    @Query(
        """
        SELECT
            l.id              AS loanId,
            l.memberId        AS memberId,
            l.principalAmount AS principalAmount,
            l.interestRatePercent AS interestRatePercent,
            l.durationMonths  AS durationMonths,
            l.startDateMillis AS startDateMillis,
            l.dueDateMillis   AS dueDateMillis,
            l.isClosed        AS isClosed,
            COALESCE(SUM(r.amount), 0) AS totalRepaid
        FROM loans l
        LEFT JOIN repayments r ON r.loanId = l.id
        GROUP BY l.id
        ORDER BY l.isClosed ASC, l.startDateMillis DESC
        """
    )
    fun getAllLoansWithRepaymentTotals(): Flow<List<LoanWithRepaymentTotal>>
}
