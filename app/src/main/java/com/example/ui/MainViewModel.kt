package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConflictStatus
import com.example.data.model.FileCategory
import com.example.data.model.PeerDevice
import com.example.data.model.StorageBreakdown
import com.example.data.model.SyncConflict
import com.example.data.model.SyncEventLog
import com.example.data.model.SyncStatus
import com.example.data.model.SyncedFile
import com.example.data.model.TransferItem
import com.example.data.repository.ConflictResolutionChoice
import com.example.data.repository.SyncBeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FileSortOrder {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    SIZE_DESC
}

enum class NavigationTab(val title: String, val testTag: String) {
    VAULT("Vault", "nav_vault"),
    RADAR("Radar", "nav_radar"),
    LIVE_SYNC("Live Sync", "nav_transfers"),
    CONFLICTS("Conflicts", "nav_conflicts"),
    STORAGE("Storage & Mesh", "nav_storage")
}

data class UiState(
    val files: List<SyncedFile> = emptyList(),
    val filteredFiles: List<SyncedFile> = emptyList(),
    val selectedCategory: FileCategory = FileCategory.ALL,
    val searchQuery: String = "",
    val sortOrder: FileSortOrder = FileSortOrder.DATE_DESC,
    val pendingConflicts: List<SyncConflict> = emptyList(),
    val resolvedConflicts: List<SyncConflict> = emptyList(),
    val activeTransfers: List<TransferItem> = emptyList(),
    val discoveredPeers: List<PeerDevice> = emptyList(),
    val recentLogs: List<SyncEventLog> = emptyList(),
    val isScanning: Boolean = false,
    val isServerListening: Boolean = true,
    val storageBreakdown: StorageBreakdown = StorageBreakdown(),
    val selectedFileForPreview: SyncedFile? = null,
    val selectedConflictForReview: SyncConflict? = null,
    val currentTab: NavigationTab = NavigationTab.VAULT,
    val isCreatingNoteDialog: Boolean = false,
    val isAddPeerDialog: Boolean = false,
    val isStorageSettingsDialog: Boolean = false,
    val quotaLimitGb: Float = 2.0f,
    val autoSyncOnConnect: Boolean = true,
    val meshRelayEnabled: Boolean = true,
    val snackbarMessage: String? = null
)

class MainViewModel(private val repository: SyncBeamRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(FileSortOrder.DATE_DESC)
    private val _currentTab = MutableStateFlow(NavigationTab.VAULT)
    private val _selectedFileForPreview = MutableStateFlow<SyncedFile?>(null)
    private val _selectedConflictForReview = MutableStateFlow<SyncConflict?>(null)
    private val _isCreatingNoteDialog = MutableStateFlow(false)
    private val _isAddPeerDialog = MutableStateFlow(false)
    private val _isStorageSettingsDialog = MutableStateFlow(false)
    private val _quotaLimitGb = MutableStateFlow(2.0f)
    private val _autoSyncOnConnect = MutableStateFlow(true)
    private val _meshRelayEnabled = MutableStateFlow(true)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> = combine(
        repository.allFiles,
        repository.pendingConflicts,
        repository.allConflicts,
        repository.activeTransfers,
        repository.discoveredPeers,
        repository.recentLogs,
        repository.bluetoothEngine.isScanning,
        repository.bluetoothEngine.isServerListening,
        _selectedCategory,
        _searchQuery,
        _sortOrder,
        _currentTab,
        _selectedFileForPreview,
        _selectedConflictForReview,
        _isCreatingNoteDialog,
        _isAddPeerDialog,
        _isStorageSettingsDialog,
        _quotaLimitGb,
        _autoSyncOnConnect,
        _meshRelayEnabled,
        _snackbarMessage
    ) { params ->
        val files = params[0] as List<SyncedFile>
        val pendingConflicts = params[1] as List<SyncConflict>
        val allConflicts = params[2] as List<SyncConflict>
        val transfers = params[3] as List<TransferItem>
        val peers = params[4] as List<PeerDevice>
        val logs = params[5] as List<SyncEventLog>
        val isScanning = params[6] as Boolean
        val isServerListening = params[7] as Boolean
        val selectedCategory = params[8] as FileCategory
        val searchQuery = params[9] as String
        val sortOrder = params[10] as FileSortOrder
        val currentTab = params[11] as NavigationTab
        val previewFile = params[12] as SyncedFile?
        val reviewConflict = params[13] as SyncConflict?
        val isCreatingNote = params[14] as Boolean
        val isAddPeer = params[15] as Boolean
        val isStorageSettings = params[16] as Boolean
        val quotaGb = params[17] as Float
        val autoSync = params[18] as Boolean
        val meshRelay = params[19] as Boolean
        val snackMsg = params[20] as String?

        val resolved = allConflicts.filter { it.status != ConflictStatus.PENDING }

        // Filter files
        val filtered = files.filter { file ->
            val matchCategory = selectedCategory == FileCategory.ALL || file.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    file.name.contains(searchQuery, ignoreCase = true) ||
                    (file.textPreview?.contains(searchQuery, ignoreCase = true) == true)
            matchCategory && matchQuery
        }.let { list ->
            when (sortOrder) {
                FileSortOrder.DATE_DESC -> list.sortedByDescending { it.lastModifiedTimestamp }
                FileSortOrder.DATE_ASC -> list.sortedBy { it.lastModifiedTimestamp }
                FileSortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                FileSortOrder.SIZE_DESC -> list.sortedByDescending { it.sizeBytes }
            }
        }

        val quotaBytes = (quotaGb * 1024 * 1024 * 1024).toLong()
        val storageBreakdown = repository.vaultManager.calculateStorageBreakdown(files, quotaBytes)
            .copy(conflictCount = pendingConflicts.size)

        UiState(
            files = files,
            filteredFiles = filtered,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            sortOrder = sortOrder,
            pendingConflicts = pendingConflicts,
            resolvedConflicts = resolved,
            activeTransfers = transfers,
            discoveredPeers = peers,
            recentLogs = logs,
            isScanning = isScanning,
            isServerListening = isServerListening,
            storageBreakdown = storageBreakdown,
            selectedFileForPreview = previewFile,
            selectedConflictForReview = reviewConflict,
            currentTab = currentTab,
            isCreatingNoteDialog = isCreatingNote,
            isAddPeerDialog = isAddPeer,
            isStorageSettingsDialog = isStorageSettings,
            quotaLimitGb = quotaGb,
            autoSyncOnConnect = autoSync,
            meshRelayEnabled = meshRelay,
            snackbarMessage = snackMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: FileCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: FileSortOrder) {
        _sortOrder.value = order
    }

    fun openPreview(file: SyncedFile) {
        _selectedFileForPreview.value = file
    }

    fun closePreview() {
        _selectedFileForPreview.value = null
    }

    fun openConflictReview(conflict: SyncConflict) {
        _selectedConflictForReview.value = conflict
    }

    fun closeConflictReview() {
        _selectedConflictForReview.value = null
    }

    fun showCreateNoteDialog(show: Boolean) {
        _isCreatingNoteDialog.value = show
    }

    fun showAddPeerDialog(show: Boolean) {
        _isAddPeerDialog.value = show
    }

    fun showStorageSettingsDialog(show: Boolean) {
        _isStorageSettingsDialog.value = show
    }

    fun setQuotaLimitGb(gb: Float) {
        _quotaLimitGb.value = gb
    }

    fun toggleAutoSync(enabled: Boolean) {
        _autoSyncOnConnect.value = enabled
    }

    fun toggleMeshRelay(enabled: Boolean) {
        _meshRelayEnabled.value = enabled
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Actions
    fun importFile(uri: Uri, name: String, categoryOverride: FileCategory? = null) {
        viewModelScope.launch {
            try {
                repository.importFileFromUri(uri, name, categoryOverride)
                showSnackbar("Imported '$name' into offline vault")
            } catch (e: Exception) {
                showSnackbar("Error importing file: ${e.localizedMessage}")
            }
        }
    }

    fun createNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                repository.createNewTextNote(title, content)
                showCreateNoteDialog(false)
                showSnackbar("Created note '$title'")
            } catch (e: Exception) {
                showSnackbar("Error saving note: ${e.localizedMessage}")
            }
        }
    }

    fun updateNote(fileId: String, newContent: String) {
        viewModelScope.launch {
            try {
                repository.updateTextDocument(fileId, newContent)
                closePreview()
                showSnackbar("Saved changes & updated vector clock")
            } catch (e: Exception) {
                showSnackbar("Error saving document: ${e.localizedMessage}")
            }
        }
    }

    fun syncFileWithPeer(file: SyncedFile, peer: PeerDevice) {
        viewModelScope.launch {
            repository.syncFileWithPeer(file, peer)
            showSnackbar("Syncing '${file.name}' with ${peer.name}...")
        }
    }

    fun syncAllFilesWithPeer(peer: PeerDevice) {
        viewModelScope.launch {
            repository.syncAllFilesWithPeer(peer)
            showSnackbar("Syncing all vault files with ${peer.name}...")
        }
    }

    fun simulateConflictTest(file: SyncedFile, peer: PeerDevice) {
        viewModelScope.launch {
            repository.triggerSimulatedConflict(file, peer)
            showSnackbar("Simulated concurrent edit conflict from ${peer.name}")
        }
    }

    fun resolveConflict(
        conflictId: String,
        choice: ConflictResolutionChoice,
        mergedContent: String? = null
    ) {
        viewModelScope.launch {
            try {
                repository.resolveConflict(conflictId, choice, mergedContent)
                closeConflictReview()
                val msg = when (choice) {
                    ConflictResolutionChoice.KEEP_LOCAL_WINNER -> "Resolved: Local copy set as winner"
                    ConflictResolutionChoice.KEEP_REMOTE_WINNER -> "Resolved: Adopted remote copy"
                    ConflictResolutionChoice.KEEP_BOTH -> "Resolved: Created duplicate branch copy"
                    ConflictResolutionChoice.MERGE_CUSTOM -> "Resolved: Merged changes successfully"
                }
                showSnackbar(msg)
            } catch (e: Exception) {
                showSnackbar("Error resolving conflict: ${e.localizedMessage}")
            }
        }
    }

    fun togglePin(fileId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePin(fileId, isPinned)
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            showSnackbar("File removed from cache vault")
        }
    }

    fun purgeCache() {
        viewModelScope.launch {
            val count = repository.purgeCache(keepPinned = true)
            showSnackbar("Purged $count unpinned files from cache")
        }
    }

    fun startBluetoothScan() {
        repository.bluetoothEngine.startDiscovery()
    }

    fun stopBluetoothScan() {
        repository.bluetoothEngine.stopDiscovery()
    }

    fun addVirtualMeshPeer(name: String, model: String) {
        repository.bluetoothEngine.addVirtualMeshPeer(name, model)
        showAddPeerDialog(false)
        showSnackbar("Added virtual Bluetooth peer '$name'")
    }

    fun togglePeerConnection(peerId: String) {
        repository.bluetoothEngine.togglePeerConnection(peerId)
    }

    fun clearCompletedTransfers() {
        repository.bluetoothEngine.clearCompletedTransfers()
    }

    class Factory(private val repository: SyncBeamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
