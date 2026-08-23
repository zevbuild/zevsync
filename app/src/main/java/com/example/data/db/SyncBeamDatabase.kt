package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.SyncConflict
import com.example.data.model.SyncEventLog
import com.example.data.model.SyncedFile

@Database(
    entities = [
        SyncedFile::class,
        SyncConflict::class,
        SyncEventLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SyncBeamDatabase : RoomDatabase() {
    abstract fun syncedFileDao(): SyncedFileDao
    abstract fun syncConflictDao(): SyncConflictDao
    abstract fun syncEventLogDao(): SyncEventLogDao

    companion object {
        @Volatile
        private var INSTANCE: SyncBeamDatabase? = null

        fun getDatabase(context: Context): SyncBeamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SyncBeamDatabase::class.java,
                    "syncbeam_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
