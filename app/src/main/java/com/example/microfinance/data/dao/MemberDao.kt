package com.example.microfinance.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.microfinance.data.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Insert
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Delete
    suspend fun delete(member: MemberEntity)

    @Query("SELECT * FROM members ORDER BY name")
    fun getAll(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members ORDER BY name")
    suspend fun getAllOnce(): List<MemberEntity>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    fun getById(memberId: Long): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    suspend fun getByIdOnce(memberId: Long): MemberEntity?

    /** Count of active (open) loans for a member — used before deletion */
    @Query(
        "SELECT COUNT(*) FROM loans WHERE memberId = :memberId AND isClosed = 0"
    )
    suspend fun getActiveLoanCountForMember(memberId: Long): Int
}
