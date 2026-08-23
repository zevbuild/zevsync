package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.bluetooth.BluetoothSyncEngine
import com.example.data.cache.CacheVaultManager
import com.example.data.cache.VectorClockComparison
import com.example.data.cache.VectorClockEngine
import com.example.data.db.SyncBeamDatabase
import com.example.data.github.GitHubDownloadResult
import com.example.data.github.GitHubFileItem
import com.example.data.github.GitHubReleaseInfo
import com.example.data.github.GitHubSyncService
import com.example.data.model.ConflictStatus
import com.example.data.model.FileCategory
import com.example.data.model.PeerDevice
import com.example.data.model.StorageBreakdown
import com.example.data.model.SyncConflict
import com.example.data.model.SyncEventLog
import com.example.data.model.SyncStatus
import com.example.data.model.SyncedFile
import com.example.data.model.TransferDirection
import com.example.data.model.TransferItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class SyncBeamRepository(
    private val context: Context,
    private val scope: CoroutineScope,
    private val database: SyncBeamDatabase,
    val vaultManager: CacheVaultManager,
    val vectorClockEngine: VectorClockEngine,
    val bluetoothEngine: BluetoothSyncEngine,
    val localDeviceId: String,
    val localDeviceName: String,
    val gitHubService: GitHubSyncService = GitHubSyncService(context.cacheDir)
) {
    private val fileDao = database.syncedFileDao()
    private val conflictDao = database.syncConflictDao()
    private val logDao = database.syncEventLogDao()

    val allFiles: Flow<List<SyncedFile>> = fileDao.getAllFiles()
    val pendingConflicts: Flow<List<SyncConflict>> = conflictDao.getPendingConflicts()
    val allConflicts: Flow<List<SyncConflict>> = conflictDao.getAllConflicts()
    val recentLogs: Flow<List<SyncEventLog>> = logDao.getRecentLogs()
    val activeTransfers: StateFlow<List<TransferItem>> = bluetoothEngine.activeTransfers
    val discoveredPeers: StateFlow<List<PeerDevice>> = bluetoothEngine.discoveredPeers

    init {
        // Collect real-time log events from Bluetooth engine into Room database
        scope.launch(Dispatchers.IO) {
            bluetoothEngine.eventLogs.collect { log ->
                logDao.insertLog(log)
            }
        }

        // Seed initial vault data if empty
        scope.launch(Dispatchers.IO) {
            seedInitialDemoVault()
        }
    }

    suspend fun importFileFromUri(uri: Uri, name: String, categoryOverride: FileCategory? = null) =
        withContext(Dispatchers.IO) {
            val (file, hash, size) = vaultManager.saveUriToVault(uri, name)
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val category = categoryOverride ?: vaultManager.determineCategory(name, mimeType)
            val textPreview = vaultManager.extractTextPreview(file)

            val (newClock, newVersion) = vectorClockEngine.incrementLocal("{}")

            val syncedFile = SyncedFile(
                id = hash,
                name = name,
                sizeBytes = size,
                mimeType = mimeType,
                contentHash = hash,
                localFilePath = file.absolutePath,
                versionNumber = newVersion,
                originDeviceId = localDeviceId,
                originDeviceName = localDeviceName,
                lastModifiedTimestamp = System.currentTimeMillis(),
                lamportTimestamp = 1L,
                vectorClockJson = newClock,
                syncStatus = SyncStatus.LOCAL_ONLY,
                category = category,
                textPreview = textPreview
            )

            fileDao.insertOrUpdate(syncedFile)
            bluetoothEngine.logEvent(
                "FILE_ADDED",
                "File Imported to Vault",
                "$name (${syncedFile.sizeFormatted}) added locally",
                null
            )
        }

    suspend fun createNewTextNote(title: String, content: String) = withContext(Dispatchers.IO) {
        val fileName = if (title.endsWith(".md") || title.endsWith(".txt")) title else "$title.md"
        val (file, hash, size) = vaultManager.saveTextDocument(fileName, content)
        val (newClock, newVersion) = vectorClockEngine.incrementLocal("{}")

        val syncedFile = SyncedFile(
            id = hash,
            name = fileName,
            sizeBytes = size,
            mimeType = "text/markdown",
            contentHash = hash,
            localFilePath = file.absolutePath,
            versionNumber = newVersion,
            originDeviceId = localDeviceId,
            originDeviceName = localDeviceName,
            lastModifiedTimestamp = System.currentTimeMillis(),
            lamportTimestamp = 1L,
            vectorClockJson = newClock,
            syncStatus = SyncStatus.LOCAL_ONLY,
            category = FileCategory.CODE,
            textPreview = content.take(1000)
        )

        fileDao.insertOrUpdate(syncedFile)
        bluetoothEngine.logEvent(
            "NOTE_CREATED",
            "Offline Note Created",
            "$fileName ($size bytes) saved to cache",
            null
        )
    }

    suspend fun updateTextDocument(fileId: String, newContent: String) = withContext(Dispatchers.IO) {
        val existing = fileDao.getFileById(fileId) ?: return@withContext
        val (newFile, newHash, newSize) = vaultManager.saveTextDocument(existing.name, newContent)
        val (newClock, newVersion) = vectorClockEngine.incrementLocal(existing.vectorClockJson)
        val nextLamport = vectorClockEngine.nextLamport(existing.lamportTimestamp)

        val updated = existing.copy(
            id = newHash,
            sizeBytes = newSize,
            contentHash = newHash,
            localFilePath = newFile.absolutePath,
            versionNumber = newVersion,
            lastModifiedTimestamp = System.currentTimeMillis(),
            lamportTimestamp = nextLamport,
            vectorClockJson = newClock,
            syncStatus = SyncStatus.LOCAL_ONLY,
            textPreview = newContent.take(1000)
        )

        if (newHash != existing.id) {
            fileDao.deletePermanently(existing.id)
        }
        fileDao.insertOrUpdate(updated)

        bluetoothEngine.logEvent(
            "FILE_UPDATED",
            "File Updated Locally",
            "${existing.name} updated to v$newVersion",
            null
        )
    }

    suspend fun downloadFromGitHub(
        urlOrPath: String,
        customName: String? = null,
        onProgress: ((Float, Long, Long) -> Unit)? = null
    ): GitHubDownloadResult = withContext(Dispatchers.IO) {
        val result = gitHubService.downloadDirectFile(
            rawOrGitHubUrl = urlOrPath,
            customFileName = customName,
            onProgress = onProgress
        )

        if (result.success && result.localFile != null) {
            val file = result.localFile
            val (savedFile, hash, size) = vaultManager.saveBytesToVault(
                name = result.fileName,
                bytes = file.readBytes()
            )
            val mimeType = vaultManager.determineMimeType(result.fileName)
            val category = vaultManager.determineCategory(result.fileName, mimeType)
            val textPreview = result.textContent?.take(1000) ?: vaultManager.extractTextPreview(savedFile)

            val (newClock, newVersion) = vectorClockEngine.incrementLocal("{}")

            val syncedFile = SyncedFile(
                id = hash,
                name = result.fileName,
                sizeBytes = size,
                mimeType = mimeType,
                contentHash = hash,
                localFilePath = savedFile.absolutePath,
                versionNumber = newVersion,
                originDeviceId = "github-import",
                originDeviceName = "GitHub Remote",
                lastModifiedTimestamp = System.currentTimeMillis(),
                lamportTimestamp = 1L,
                vectorClockJson = newClock,
                syncStatus = SyncStatus.LOCAL_ONLY,
                category = category,
                textPreview = textPreview
            )

            fileDao.insertOrUpdate(syncedFile)
            bluetoothEngine.logEvent(
                "GITHUB_IMPORT",
                "Downloaded from GitHub",
                "${result.fileName} (${syncedFile.sizeFormatted}) ready for offline mesh sync",
                "GitHub Cloud"
            )
        }

        result
    }

    suspend fun fetchGitHubReleases(owner: String, repo: String): Result<GitHubReleaseInfo> =
        withContext(Dispatchers.IO) {
            gitHubService.getLatestRelease(owner, repo)
        }

    suspend fun fetchGitHubContents(owner: String, repo: String, path: String = ""): Result<List<GitHubFileItem>> =
        withContext(Dispatchers.IO) {
            gitHubService.listRepositoryContents(owner, repo, path)
        }

    suspend fun exportVaultFileToGist(
        file: SyncedFile,
        isPublic: Boolean = false,
        token: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val content = vaultManager.readTextDocument(file.localFilePath)
            ?: file.textPreview
            ?: return@withContext Result.failure(Exception("File content cannot be read as text for Gist"))

        gitHubService.exportToGist(
            fileName = file.name,
            content = content,
            description = "Exported from SyncBeam Mesh Vault (${file.name})",
            isPublic = isPublic,
            token = token
        )
    }

    suspend fun syncFileWithPeer(file: SyncedFile, peer: PeerDevice) = withContext(Dispatchers.IO) {
        bluetoothEngine.simulateLiveFileTransfer(
            targetFile = file,
            peer = peer,
            direction = TransferDirection.UPLOAD
        ) { updatedFile ->
            scope.launch(Dispatchers.IO) {
                fileDao.insertOrUpdate(updatedFile)
            }
        }
    }

    suspend fun syncAllFilesWithPeer(peer: PeerDevice) = withContext(Dispatchers.IO) {
        val files = fileDao.getEvictionCandidates()
        bluetoothEngine.logEvent(
            "BATCH_SYNC",
            "Batch Sync Initiated",
            "Broadcasting catalog to ${peer.name} (${files.size} files)",
            peer.name
        )
        for (file in files.take(4)) {
            syncFileWithPeer(file, peer)
        }
    }

    suspend fun triggerSimulatedConflict(file: SyncedFile, peer: PeerDevice) = withContext(Dispatchers.IO) {
        // Creates a realistic concurrent edit simulation from a nearby peer
        val remoteVersion = file.versionNumber + 1
        val remoteDeviceId = peer.id
        val remoteDeviceName = peer.name
        val remoteLamport = file.lamportTimestamp + 2
        val remoteClock = vectorClockEngine.serializeClock(
            mapOf(remoteDeviceId to remoteVersion, localDeviceId to file.versionNumber)
        )
        val remoteText = (file.textPreview ?: "Document data...") + "\n\n[Remote modification by $remoteDeviceName at ${System.currentTimeMillis()}]"
        val remoteHash = "rem_" + UUID.randomUUID().toString().take(12)

        val conflict = SyncConflict(
            id = UUID.randomUUID().toString(),
            fileId = file.id,
            fileName = file.name,
            localVersion = file.versionNumber,
            remoteVersion = remoteVersion,
            localHash = file.contentHash,
            remoteHash = remoteHash,
            localTimestamp = file.lastModifiedTimestamp,
            remoteTimestamp = System.currentTimeMillis() - 60000,
            localDeviceId = file.originDeviceId,
            remoteDeviceId = remoteDeviceId,
            remoteDeviceName = remoteDeviceName,
            localSizeBytes = file.sizeBytes,
            remoteSizeBytes = file.sizeBytes + 120,
            localFilePath = file.localFilePath,
            remoteFilePath = null,
            localLamport = file.lamportTimestamp,
            remoteLamport = remoteLamport,
            localVectorClock = file.vectorClockJson,
            remoteVectorClock = remoteClock,
            localTextSnippet = file.textPreview ?: "Local document content",
            remoteTextSnippet = remoteText,
            status = ConflictStatus.PENDING
        )

        conflictDao.insertConflict(conflict)
        fileDao.updateSyncStatus(file.id, SyncStatus.CONFLICT)

        bluetoothEngine.logEvent(
            "CONFLICT_DETECTED",
            "Sync Conflict on ${file.name}",
            "Concurrent edits from $remoteDeviceName vs local device detected",
            remoteDeviceName,
            isPositive = false
        )
    }

    suspend fun resolveConflict(
        conflictId: String,
        action: ConflictResolutionChoice,
        mergedContent: String? = null
    ) = withContext(Dispatchers.IO) {
        val conflict = conflictDao.getConflictById(conflictId) ?: return@withContext
        val existingFile = fileDao.getFileById(conflict.fileId)

        when (action) {
            ConflictResolutionChoice.KEEP_LOCAL_WINNER -> {
                val (mergedClock, _) = vectorClockEngine.incrementLocal(
                    vectorClockEngine.merge(conflict.localVectorClock, conflict.remoteVectorClock)
                )
                existingFile?.let {
                    val resolvedFile = it.copy(
                        syncStatus = SyncStatus.SYNCED,
                        vectorClockJson = mergedClock,
                        lastModifiedTimestamp = System.currentTimeMillis()
                    )
                    fileDao.insertOrUpdate(resolvedFile)
                }
                conflictDao.resolveConflict(
                    conflictId,
                    ConflictStatus.RESOLVED_LOCAL,
                    System.currentTimeMillis(),
                    "Local version chosen as authoritative winner."
                )
                bluetoothEngine.logEvent(
                    "CONFLICT_RESOLVED",
                    "Conflict Resolved: Local Winner",
                    "${conflict.fileName} marked local version as winner",
                    conflict.remoteDeviceName
                )
            }

            ConflictResolutionChoice.KEEP_REMOTE_WINNER -> {
                val mergedClock = vectorClockEngine.merge(conflict.localVectorClock, conflict.remoteVectorClock)
                val (finalFile, finalHash, finalSize) = if (conflict.remoteTextSnippet != null) {
                    vaultManager.saveTextDocument(conflict.fileName, conflict.remoteTextSnippet)
                } else {
                    Triple(existingFile?.localFilePath?.let { File(it) } ?: File(""), conflict.remoteHash, conflict.remoteSizeBytes)
                }

                existingFile?.let {
                    val resolvedFile = it.copy(
                        id = finalHash,
                        contentHash = finalHash,
                        sizeBytes = finalSize,
                        versionNumber = conflict.remoteVersion,
                        localFilePath = finalFile.absolutePath,
                        syncStatus = SyncStatus.SYNCED,
                        vectorClockJson = mergedClock,
                        lastModifiedTimestamp = conflict.remoteTimestamp,
                        textPreview = conflict.remoteTextSnippet?.take(1000)
                    )
                    if (finalHash != it.id) {
                        fileDao.deletePermanently(it.id)
                    }
                    fileDao.insertOrUpdate(resolvedFile)
                }
                conflictDao.resolveConflict(
                    conflictId,
                    ConflictStatus.RESOLVED_REMOTE,
                    System.currentTimeMillis(),
                    "Remote peer version adopted."
                )
                bluetoothEngine.logEvent(
                    "CONFLICT_RESOLVED",
                    "Conflict Resolved: Remote Adopted",
                    "${conflict.fileName} replaced with ${conflict.remoteDeviceName}'s copy",
                    conflict.remoteDeviceName
                )
            }

            ConflictResolutionChoice.KEEP_BOTH -> {
                // Keep local as is, save remote as a branch file
                val branchName = "${conflict.fileName.substringBeforeLast('.')}_(From_${conflict.remoteDeviceName}).${conflict.fileName.substringAfterLast('.', "txt")}"
                val branchContent = conflict.remoteTextSnippet ?: "Branch copy from ${conflict.remoteDeviceName}"
                val (bFile, bHash, bSize) = vaultManager.saveTextDocument(branchName, branchContent)

                val branchFile = SyncedFile(
                    id = bHash,
                    name = branchName,
                    sizeBytes = bSize,
                    mimeType = existingFile?.mimeType ?: "text/plain",
                    contentHash = bHash,
                    localFilePath = bFile.absolutePath,
                    versionNumber = conflict.remoteVersion,
                    originDeviceId = conflict.remoteDeviceId,
                    originDeviceName = conflict.remoteDeviceName,
                    lastModifiedTimestamp = conflict.remoteTimestamp,
                    lamportTimestamp = conflict.remoteLamport,
                    vectorClockJson = conflict.remoteVectorClock,
                    syncStatus = SyncStatus.SYNCED,
                    category = existingFile?.category ?: FileCategory.OTHER,
                    textPreview = branchContent.take(1000)
                )

                fileDao.insertOrUpdate(branchFile)
                existingFile?.let {
                    fileDao.insertOrUpdate(it.copy(syncStatus = SyncStatus.SYNCED))
                }

                conflictDao.resolveConflict(
                    conflictId,
                    ConflictStatus.RESOLVED_KEEP_BOTH,
                    System.currentTimeMillis(),
                    "Both versions kept; created $branchName"
                )
                bluetoothEngine.logEvent(
                    "CONFLICT_RESOLVED",
                    "Conflict Resolved: Both Kept",
                    "Forked separate branch file: $branchName",
                    conflict.remoteDeviceName
                )
            }

            ConflictResolutionChoice.MERGE_CUSTOM -> {
                val mergedText = mergedContent ?: conflict.localTextSnippet ?: ""
                val (mFile, mHash, mSize) = vaultManager.saveTextDocument(conflict.fileName, mergedText)
                val mergedClock = vectorClockEngine.merge(conflict.localVectorClock, conflict.remoteVectorClock)
                val (finalClock, finalVersion) = vectorClockEngine.incrementLocal(mergedClock)

                existingFile?.let {
                    val resolvedFile = it.copy(
                        id = mHash,
                        contentHash = mHash,
                        sizeBytes = mSize,
                        versionNumber = finalVersion,
                        localFilePath = mFile.absolutePath,
                        syncStatus = SyncStatus.SYNCED,
                        vectorClockJson = finalClock,
                        lastModifiedTimestamp = System.currentTimeMillis(),
                        textPreview = mergedText.take(1000)
                    )
                    if (mHash != it.id) {
                        fileDao.deletePermanently(it.id)
                    }
                    fileDao.insertOrUpdate(resolvedFile)
                }

                conflictDao.resolveConflict(
                    conflictId,
                    ConflictStatus.RESOLVED_MERGED,
                    System.currentTimeMillis(),
                    "Manual side-by-side merge completed."
                )
                bluetoothEngine.logEvent(
                    "CONFLICT_RESOLVED",
                    "Conflict Resolved: Manual Merge",
                    "Merged changes for ${conflict.fileName}",
                    conflict.remoteDeviceName
                )
            }
        }
    }

    suspend fun togglePin(fileId: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        fileDao.updatePinned(fileId, isPinned)
        bluetoothEngine.logEvent(
            "CACHE_PIN",
            if (isPinned) "File Pinned" else "File Unpinned",
            if (isPinned) "File protected from cache eviction" else "File eligible for cache eviction",
            null
        )
    }

    suspend fun deleteFile(fileId: String) = withContext(Dispatchers.IO) {
        val file = fileDao.getFileById(fileId)
        file?.localFilePath?.let { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {}
        }
        fileDao.deletePermanently(fileId)
        bluetoothEngine.logEvent(
            "FILE_DELETED",
            "File Removed from Vault",
            "${file?.name ?: fileId} removed from local storage",
            null
        )
    }

    suspend fun purgeCache(keepPinned: Boolean = true): Int = withContext(Dispatchers.IO) {
        val candidates = fileDao.getEvictionCandidates()
        var purgedCount = 0
        for (c in candidates) {
            c.localFilePath?.let { path ->
                try {
                    File(path).delete()
                    purgedCount++
                } catch (e: Exception) {}
            }
            fileDao.deletePermanently(c.id)
        }
        bluetoothEngine.logEvent(
            "CACHE_PURGED",
            "Cache Storage Purged",
            "Evicted $purgedCount cached files to free device memory",
            null
        )
        purgedCount
    }

    private suspend fun seedInitialDemoVault() {
        val count = fileDao.getActiveFileCount()
        // Check if DB already populated
        val existingFiles = fileDao.getEvictionCandidates()
        if (existingFiles.isNotEmpty()) return

        // 1. Markdown Project Specs Note
        val noteContent = """
            # 🚀 SyncBeam Offline Bluetooth Mesh
            
            Welcome to SyncBeam! This application creates an autonomous offline peer-to-peer sharing and cache environment without needing any internet connection, cellular data, or central servers.
            
            ## Key Features:
            - **Automatic Bluetooth Handshake**: Automatically detects nearby paired and discoverable Bluetooth devices.
            - **Vector Clock & Lamport Lineage**: Seamless multi-device causality tracking.
            - **Conflict Management Engine**: Intelligent LWW, side-by-side interactive merging, and branch-forking.
            - **Chunked Content-Addressable Storage**: SHA-256 integrity checks with resumable block transfers.
            - **Smart Cache & Quota Controller**: Configurable storage thresholds with LRU auto-eviction and pin protection.
        """.trimIndent()
        val (f1, h1, s1) = vaultManager.saveTextDocument("Mesh_Architecture_Guide.md", noteContent)
        val (clock1, v1) = vectorClockEngine.incrementLocal("{}")

        // 2. Sample Config JSON
        val jsonContent = """
            {
              "mesh_name": "SyncBeam-Local-Mesh",
              "rfcomm_channel": 1,
              "protocol_version": 2,
              "chunk_size_kb": 64,
              "auto_sync_on_connect": true,
              "conflict_policy": "PROMPT_INTERACTIVE",
              "cache_quota_gb": 2.0,
              "eviction_strategy": "LRU_UNPINNED"
            }
        """.trimIndent()
        val (f2, h2, s2) = vaultManager.saveTextDocument("mesh_config.json", jsonContent)
        val (clock2, v2) = vectorClockEngine.incrementLocal("{}")

        // 3. Kotlin Sync Algorithm Sample
        val ktContent = """
            package sync.mesh
            
            // Vector Clock Conflict Resolver
            fun resolveLineage(local: Map<String, Long>, remote: Map<String, Long>): ConflictResult {
                val lGreater = local.any { (k, v) -> v > (remote[k] ?: 0L) }
                val rGreater = remote.any { (k, v) -> v > (local[k] ?: 0L) }
                return when {
                    lGreater && !rGreater -> ConflictResult.LOCAL_DOMINATES
                    !lGreater && rGreater -> ConflictResult.REMOTE_DOMINATES
                    !lGreater && !rGreater -> ConflictResult.EQUAL
                    else -> ConflictResult.CONCURRENT_CONFLICT
                }
            }
        """.trimIndent()
        val (f3, h3, s3) = vaultManager.saveTextDocument("ConflictResolver.kt", ktContent)
        val (clock3, v3) = vectorClockEngine.incrementLocal("{}")

        val files = listOf(
            SyncedFile(
                id = h1,
                name = "Mesh_Architecture_Guide.md",
                sizeBytes = s1,
                mimeType = "text/markdown",
                contentHash = h1,
                localFilePath = f1.absolutePath,
                versionNumber = v1,
                originDeviceId = localDeviceId,
                originDeviceName = localDeviceName,
                lastModifiedTimestamp = System.currentTimeMillis() - 3600000,
                lamportTimestamp = 1L,
                vectorClockJson = clock1,
                syncStatus = SyncStatus.SYNCED,
                isPinned = true,
                category = FileCategory.DOCUMENT,
                textPreview = noteContent.take(1000)
            ),
            SyncedFile(
                id = h2,
                name = "mesh_config.json",
                sizeBytes = s2,
                mimeType = "application/json",
                contentHash = h2,
                localFilePath = f2.absolutePath,
                versionNumber = v2,
                originDeviceId = localDeviceId,
                originDeviceName = localDeviceName,
                lastModifiedTimestamp = System.currentTimeMillis() - 7200000,
                lamportTimestamp = 2L,
                vectorClockJson = clock2,
                syncStatus = SyncStatus.SYNCED,
                category = FileCategory.CODE,
                textPreview = jsonContent.take(1000)
            ),
            SyncedFile(
                id = h3,
                name = "ConflictResolver.kt",
                sizeBytes = s3,
                mimeType = "text/x-kotlin",
                contentHash = h3,
                localFilePath = f3.absolutePath,
                versionNumber = v3,
                originDeviceId = localDeviceId,
                originDeviceName = localDeviceName,
                lastModifiedTimestamp = System.currentTimeMillis() - 1800000,
                lamportTimestamp = 3L,
                vectorClockJson = clock3,
                syncStatus = SyncStatus.LOCAL_ONLY,
                category = FileCategory.CODE,
                textPreview = ktContent.take(1000)
            )
        )

        fileDao.insertAll(files)

        // Add 2 default nearby peer nodes for instant interaction
        bluetoothEngine.addVirtualMeshPeer("Pixel 9 Pro (Living Room)", "Google Pixel 9 Pro")
        bluetoothEngine.addVirtualMeshPeer("Galaxy Tab S9 (Studio)", "Samsung Galaxy Tab S9")

        // Seed a sample conflict so the user can immediately try out the conflict resolver
        triggerSimulatedConflict(files[0], bluetoothEngine.discoveredPeers.value.first())
    }
}

enum class ConflictResolutionChoice {
    KEEP_LOCAL_WINNER,
    KEEP_REMOTE_WINNER,
    KEEP_BOTH,
    MERGE_CUSTOM
}
