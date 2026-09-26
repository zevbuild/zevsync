package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConflictStatus
import com.example.data.model.SyncConflict
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncConflictDao {

    @Query("SELECT * FROM sync_conflicts WHERE status = 'PENDING' ORDER BY remoteTimestamp DESC")
    fun getPendingConflicts(): Flow<List<SyncConflict>>

    @Query("SELECT * FROM sync_conflicts ORDER BY remoteTimestamp DESC")
    fun getAllConflicts(): Flow<List<SyncConflict>>

    @Query("SELECT COUNT(*) FROM sync_conflicts WHERE status = 'PENDING'")
    fun getPendingConflictCount(): Flow<Int>

    @Query("SELECT * FROM sync_conflicts WHERE id = :id LIMIT 1")
    suspend fun getConflictById(id: String): SyncConflict?

    @Query("SELECT * FROM sync_conflicts WHERE fileId = :fileId AND status = 'PENDING' LIMIT 1")
    suspend fun getPendingConflictForFile(fileId: String): SyncConflict?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflict)

    @Update
    suspend fun updateConflict(conflict: SyncConflict)

    @Query("UPDATE sync_conflicts SET status = :status, resolutionTimestamp = :timestamp, resolutionNote = :note WHERE id = :id")
    suspend fun resolveConflict(id: String, status: ConflictStatus, timestamp: Long, note: String?)

    @Query("DELETE FROM sync_conflicts WHERE id = :id")
    suspend fun deleteConflict(id: String)
}
