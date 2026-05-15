package com.example.microfinance.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton row (id always = 1) storing group-level configuration.
 */
@Entity(tableName = "group_settings")
data class GroupSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val groupName: String = "Mahila-Shakti Unnati",
    /** Weekly savings contribution per member in ₹ */
    val weeklyContribution: Double = 500.0,
    /** Epoch millis when the SHG was formed / tracking started */
    val groupStartMillis: Long = System.currentTimeMillis()
)
