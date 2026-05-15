package com.example.microfinance.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.microfinance.data.entity.GroupSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupSettingsDao {

    /** Insert or replace the singleton settings row */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: GroupSettingsEntity)

    @Update
    suspend fun update(settings: GroupSettingsEntity)

    @Query("SELECT * FROM group_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<GroupSettingsEntity?>

    @Query("SELECT * FROM group_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): GroupSettingsEntity?
}
