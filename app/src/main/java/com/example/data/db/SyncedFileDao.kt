package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FileCategory
import com.example.data.model.SyncStatus
import com.example.data.model.SyncedFile
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncedFileDao {

    @Query("SELECT * FROM synced_files WHERE isDeleted = 0 ORDER BY lastModifiedTimestamp DESC")
    fun getAllFiles(): Flow<List<SyncedFile>>

    @Query("SELECT * FROM synced_files WHERE isDeleted = 0 AND category = :category ORDER BY lastModifiedTimestamp DESC")
    fun getFilesByCategory(category: FileCategory): Flow<List<SyncedFile>>

    @Query("SELECT * FROM synced_files WHERE isDeleted = 0 AND (name LIKE '%' || :query || '%' OR textPreview LIKE '%' || :query || '%') ORDER BY lastModifiedTimestamp DESC")
    fun searchFiles(query: String): Flow<List<SyncedFile>>

    @Query("SELECT * FROM synced_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): SyncedFile?

    @Query("SELECT * FROM synced_files WHERE contentHash = :hash LIMIT 1")
    suspend fun getFileByHash(hash: String): SyncedFile?

    @Query("SELECT * FROM synced_files WHERE name = :name AND relativePath = :relativePath AND isDeleted = 0 LIMIT 1")
    suspend fun getFileByNameAndPath(name: String, relativePath: String): SyncedFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(file: SyncedFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<SyncedFile>)

    @Update
    suspend fun update(file: SyncedFile)

    @Query("UPDATE synced_files SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE synced_files SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("UPDATE synced_files SET isDeleted = 1 WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("DELETE FROM synced_files WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("SELECT * FROM synced_files WHERE isDeleted = 0 AND isPinned = 0 ORDER BY lastModifiedTimestamp ASC")
    suspend fun getEvictionCandidates(): List<SyncedFile>

    @Query("SELECT SUM(sizeBytes) FROM synced_files WHERE isDeleted = 0 AND localFilePath IS NOT NULL")
    fun getTotalStorageUsed(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM synced_files WHERE isDeleted = 0")
    fun getActiveFileCount(): Flow<Int>
}
