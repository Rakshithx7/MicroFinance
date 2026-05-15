package com.example.microfinance.data.db

import androidx.room.TypeConverter
import com.example.microfinance.data.entity.SavingsStatus

class Converters {
    @TypeConverter
    fun fromSavingsStatus(value: SavingsStatus): String = value.name

    @TypeConverter
    fun toSavingsStatus(value: String): SavingsStatus = SavingsStatus.valueOf(value)
}
