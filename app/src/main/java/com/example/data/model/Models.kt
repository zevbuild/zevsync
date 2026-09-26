package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

enum class FileCategory(val label: String, val iconName: String) {
    ALL("All Files", "folder"),
    IMAGE("Images", "image"),
    DOCUMENT("Docs & PDF", "description"),
    AUDIO("Audio", "audiotrack"),
    VIDEO("Videos", "movie"),
    CODE("Code & Data", "code"),
    ARCHIVE("Archives", "folder_zip"),
    OTHER("Other", "insert_drive_file")
}

enum class SyncStatus {
    SYNCED,
    SYNCING,
    CONFLICT,
    LOCAL_ONLY,
    REMOTE_ONLY,
    QUEUED,
    ERROR
}

enum class ConflictStatus {
    PENDING,
    RESOLVED_LOCAL,
    RESOLVED_REMOTE,
    RESOLVED_KEEP_BOTH,
    RESOLVED_MERGED
}

@Entity(tableName = "synced_files")
data class SyncedFile(
    @PrimaryKey val id: String, // SHA-256 or UUID
    val name: String,
    val relativePath: String = "",
    val sizeBytes: Long,
    val mimeType: String,
    val contentHash: String, // SHA-256
    val localFilePath: String?, // Absolute path in cache vault
    val versionNumber: Long = 1,
    val originDeviceId: String,
    val originDeviceName: String,
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val lamportTimestamp: Long = 1,
    val vectorClockJson: String = "{}", // JSON map of deviceId -> version
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val isPinned: Boolean = false,
    val category: FileCategory = FileCategory.OTHER,
    val conflictWinnerId: String? = null,
    val textPreview: String? = null,
    val isDeleted: Boolean = false,
    val lastSyncPeerName: String? = null
) {
    val sizeFormatted: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                kb >= 1.0 -> String.format("%.1f KB", kb)
                else -> "$sizeBytes B"
            }
        }

    val existsLocally: Boolean
        get() = !localFilePath.isNullOrEmpty() && File(localFilePath).exists()
}

@Entity(tableName = "sync_conflicts")
data class SyncConflict(
    @PrimaryKey val id: String,
    val fileId: String,
    val fileName: String,
    val localVersion: Long,
    val remoteVersion: Long,
    val localHash: String,
    val remoteHash: String,
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val localDeviceId: String,
    val remoteDeviceId: String,
    val remoteDeviceName: String,
    val localSizeBytes: Long,
    val remoteSizeBytes: Long,
    val localFilePath: String?,
    val remoteFilePath: String?,
    val localLamport: Long,
    val remoteLamport: Long,
    val localVectorClock: String,
    val remoteVectorClock: String,
    val localTextSnippet: String? = null,
    val remoteTextSnippet: String? = null,
    val status: ConflictStatus = ConflictStatus.PENDING,
    val resolutionTimestamp: Long? = null,
    val resolutionNote: String? = null
)

@Entity(tableName = "sync_event_logs")
data class SyncEventLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // "DISCOVERY", "CONNECT", "HANDSHAKE", "TRANSFER_START", "TRANSFER_SUCCESS", "CONFLICT_DETECTED", "CACHE_EVICTED"
    val title: String,
    val description: String,
    val peerName: String? = null,
    val isPositive: Boolean = true
)

data class PeerDevice(
    val id: String,
    val bluetoothAddress: String,
    val name: String,
    val isConnected: Boolean = false,
    val isPaired: Boolean = false,
    val rssi: Int = -60, // dBm
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val totalBytesSent: Long = 0L,
    val totalBytesReceived: Long = 0L,
    val activeTransfers: Int = 0,
    val isSyncAllowed: Boolean = true,
    val isVirtualNode: Boolean = false,
    val meshHopCount: Int = 1,
    val deviceModel: String = "Android Device",
    val protocolVersion: Int = 2
)

enum class TransferDirection {
    UPLOAD,
    DOWNLOAD
}

enum class TransferStatus {
    IDLE,
    TRANSFERRING,
    PAUSED,
    COMPLETED,
    FAILED,
    VERIFYING
}

data class TransferItem(
    val id: String,
    val fileId: String,
    val fileName: String,
    val direction: TransferDirection,
    val peerId: String,
    val peerName: String,
    val totalBytes: Long,
    val bytesTransferred: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val status: TransferStatus = TransferStatus.IDLE,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null
) {
    val speedFormatted: String
        get() {
            val kb = speedBytesPerSec / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format("%.2f MB/s", mb)
                kb >= 1.0 -> String.format("%.1f KB/s", kb)
                else -> "$speedBytesPerSec B/s"
            }
        }
}

data class StorageBreakdown(
    val totalVaultBytes: Long = 0L,
    val quotaBytes: Long = 2L * 1024 * 1024 * 1024, // Default 2 GB
    val imagesBytes: Long = 0L,
    val docsBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val videoBytes: Long = 0L,
    val codeBytes: Long = 0L,
    val archiveBytes: Long = 0L,
    val otherBytes: Long = 0L,
    val fileCount: Int = 0,
    val conflictCount: Int = 0,
    val cachedPeerCopiesCount: Int = 0
) {
    val usedPercentage: Float
        get() = if (quotaBytes > 0) (totalVaultBytes.toFloat() / quotaBytes).coerceIn(0f, 1f) else 0f
}
