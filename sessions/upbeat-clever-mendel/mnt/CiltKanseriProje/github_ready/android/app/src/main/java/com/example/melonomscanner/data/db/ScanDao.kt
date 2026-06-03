package com.example.melonomscanner.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE flagged = 1 ORDER BY timestamp DESC")
    fun observeFlagged(): Flow<List<ScanEntity>>

    @Query("SELECT COUNT(*) FROM scans")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT lesionGroupId) FROM scans WHERE lesionGroupId IS NOT NULL")
    fun observeFollowedLesionCount(): Flow<Int>

    @Query("SELECT * FROM scans WHERE id = :id")
    suspend fun getById(id: Long): ScanEntity?

    @Query("SELECT * FROM scans WHERE lesionGroupId = :groupId ORDER BY timestamp ASC")
    suspend fun getByGroup(groupId: String): List<ScanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanEntity): Long

    @Update
    suspend fun update(scan: ScanEntity)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scans")
    suspend fun deleteAll()
}
