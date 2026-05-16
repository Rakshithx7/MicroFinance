package com.example.microfinance.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.microfinance.data.entity.RepaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepaymentDao {

    @Insert
    suspend fun insert(repayment: RepaymentEntity): Long

    @Query("SELECT * FROM repayments ORDER BY paidAtMillis DESC")
    suspend fun getAllOnce(): List<RepaymentEntity>

    @Query("SELECT * FROM repayments WHERE loanId = :loanId ORDER BY paidAtMillis DESC")
    fun getByLoan(loanId: Long): Flow<List<RepaymentEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM repayments WHERE loanId = :loanId")
    fun getTotalRepaidForLoan(loanId: Long): Flow<Double>

    /** One-shot suspend version — used inside coroutines to avoid Flow leak */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM repayments WHERE loanId = :loanId")
    suspend fun getTotalRepaidForLoanOnce(loanId: Long): Double

    /** Total repaid across ALL loans (group-wide) */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM repayments")
    fun getGroupTotalRepaid(): Flow<Double>

    /** Total repaid for a specific member across all their loans */
    @Query(
        """
        SELECT COALESCE(SUM(r.amount), 0)
        FROM repayments r
        INNER JOIN loans l ON r.loanId = l.id
        WHERE l.memberId = :memberId
        """
    )
    fun getTotalRepaidForMember(memberId: Long): Flow<Double>
}
