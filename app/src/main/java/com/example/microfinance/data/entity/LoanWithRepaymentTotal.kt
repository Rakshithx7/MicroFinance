package com.example.microfinance.data.entity

/**
 * Room projection — a loan row joined with its total repaid amount.
 * Used for drill-down analytics screens.
 */
data class LoanWithRepaymentTotal(
    val loanId: Long,
    val memberId: Long,
    val principalAmount: Double,
    val interestRatePercent: Double,
    val durationMonths: Int,
    val startDateMillis: Long,
    val dueDateMillis: Long,
    val isClosed: Boolean,
    val totalRepaid: Double
) {
    /** Time-based simple interest: SI = P × R × T / 100 */
    val interestAmount: Double
        get() = principalAmount * interestRatePercent * maxOf(durationMonths, 1) / 100.0

    val totalDue: Double
        get() = principalAmount + interestAmount

    val outstanding: Double
        get() = (totalDue - totalRepaid).coerceAtLeast(0.0)

    val repaymentProgressPercent: Float
        get() = if (totalDue <= 0) 1f else (totalRepaid / totalDue).toFloat().coerceIn(0f, 1f)

    val isOverdue: Boolean
        get() = !isClosed && dueDateMillis > 0 && dueDateMillis < System.currentTimeMillis()
}
