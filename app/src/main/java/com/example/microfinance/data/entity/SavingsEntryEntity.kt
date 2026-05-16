package com.example.microfinance.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_entries",
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
data class SavingsEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val amount: Double,
    val status: SavingsStatus,
    val entryDateMillis: Long,
    /** ISO week-of-year (1–53) for weekly tracking */
    val weekNumber: Int = 0,
    /** 4-digit year for the weekNumber above */
    val weekYear: Int = 0
)
