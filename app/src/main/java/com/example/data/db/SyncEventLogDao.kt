package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SyncEventLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncEventLogDao {

    @Query("SELECT * FROM sync_event_logs ORDER BY timestamp DESC LIMIT 150")
    fun getRecentLogs(): Flow<List<SyncEventLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SyncEventLog)

    @Query("DELETE FROM sync_event_logs")
    suspend fun clearLogs()
}
