package com.example.microfinance.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("memberId")]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val principalAmount: Double,
    val interestRatePercent: Double,
    val startDateMillis: Long,
    /** Loan tenure in months (0 = open-ended) */
    val durationMonths: Int = 0,
    /** Computed due date millis; 0 if open-ended */
    val dueDateMillis: Long = 0L,
    val isClosed: Boolean = false
)
