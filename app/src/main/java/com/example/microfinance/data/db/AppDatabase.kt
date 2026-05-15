package com.example.microfinance.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.microfinance.data.dao.GroupSettingsDao
import com.example.microfinance.data.dao.LoanDao
import com.example.microfinance.data.dao.MemberDao
import com.example.microfinance.data.dao.RepaymentDao
import com.example.microfinance.data.dao.SavingsDao
import com.example.microfinance.data.entity.GroupSettingsEntity
import com.example.microfinance.data.entity.LoanEntity
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.data.entity.RepaymentEntity
import com.example.microfinance.data.entity.SavingsEntryEntity

@Database(
    entities = [
        MemberEntity::class,
        SavingsEntryEntity::class,
        LoanEntity::class,
        RepaymentEntity::class,
        GroupSettingsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun savingsDao(): SavingsDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao
    abstract fun groupSettingsDao(): GroupSettingsDao
}
