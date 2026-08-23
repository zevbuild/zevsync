package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.PeerDevice
import com.example.data.model.SyncStatus
import com.example.data.model.SyncedFile
import com.example.ui.FileSortOrder
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.FileCategoryIcon
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.MeshCyan80
import com.example.ui.theme.MeshDanger
import com.example.ui.theme.MeshIndigo80
import com.example.ui.theme.MeshSuccess
import com.example.ui.theme.MeshWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    onNavigateToRadar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPeerPickerForFile by remember { mutableStateOf<SyncedFile?>(null) }
    var showSimulateConflictForFile by remember { mutableStateOf<SyncedFile?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // System file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "imported_file_${System.currentTimeMillis()}"
            viewModel.importFile(it, fileName)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Banner
            VaultHeaderBanner(
                fileCount = uiState.files.size,
                totalStorage = uiState.storageBreakdown.totalVaultBytes,
                activePeersCount = uiState.discoveredPeers.count { it.isConnected },
                onNavigateToRadar = onNavigateToRadar
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Sort
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search vault & code contents...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_files_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Sort files",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest first") },
                            onClick = {
                                viewModel.setSortOrder(FileSortOrder.DATE_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest first") },
                            onClick = {
                                viewModel.setSortOrder(FileSortOrder.DATE_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("File Name (A-Z)") },
                            onClick = {
                                viewModel.setSortOrder(FileSortOrder.NAME_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Size (Largest)") },
                            onClick = {
                                viewModel.setSortOrder(FileSortOrder.SIZE_DESC)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(FileCategory.values()) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Files List
            if (uiState.filteredFiles.isEmpty()) {
                EmptyVaultView(
                    hasSearch = uiState.searchQuery.isNotBlank(),
                    onAddNote = { viewModel.showCreateNoteDialog(true) },
                    onImportFile = { filePickerLauncher.launch("*/*") }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredFiles, key = { it.id }) { file ->
                        VaultFileCard(
                            file = file,
                            onPreview = { viewModel.openPreview(file) },
                            onSync = { showPeerPickerForFile = file },
                            onSimulateConflict = { showSimulateConflictForFile = file },
                            onTogglePin = { viewModel.togglePin(file.id, !file.isPinned) },
                            onDelete = { viewModel.deleteFile(file.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("import_file_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = "Import file from device",
                    modifier = Modifier.size(22.dp)
                )
            }

            FloatingActionButton(
                onClick = { viewModel.showCreateNoteDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_note_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Create note or code",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New File", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // Peer Picker Dialog for Live Bluetooth Sync
    showPeerPickerForFile?.let { file ->
        PeerPickerDialog(
            peers = uiState.discoveredPeers,
            fileName = file.name,
            onDismiss = { showPeerPickerForFile = null },
            onSelectPeer = { peer ->
                viewModel.syncFileWithPeer(file, peer)
                showPeerPickerForFile = null
            },
            onNavigateToRadar = {
                showPeerPickerForFile = null
                onNavigateToRadar()
            }
        )
    }

    // Simulate Conflict Picker
    showSimulateConflictForFile?.let { file ->
        SimulateConflictDialog(
            peers = uiState.discoveredPeers,
            fileName = file.name,
            onDismiss = { showSimulateConflictForFile = null },
            onSelectPeer = { peer ->
                viewModel.simulateConflictTest(file, peer)
                showSimulateConflictForFile = null
            }
        )
    }

    // Create Note Modal
    if (uiState.isCreatingNoteDialog) {
        CreateNoteDialog(
            onDismiss = { viewModel.showCreateNoteDialog(false) },
            onCreate = { title, content -> viewModel.createNote(title, content) }
        )
    }
}

@Composable
private fun VaultHeaderBanner(
    fileCount: Int,
    totalStorage: Long,
    activePeersCount: Int,
    onNavigateToRadar: () -> Unit
) {
    val totalFormatted = when {
        totalStorage >= 1024 * 1024 * 1024 -> String.format("%.2f GB", totalStorage / (1024.0 * 1024 * 1024))
        totalStorage >= 1024 * 1024 -> String.format("%.1f MB", totalStorage / (1024.0 * 1024))
        totalStorage >= 1024 -> String.format("%.1f KB", totalStorage / 1024.0)
        else -> "$totalStorage B"
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Offline Vault Cache",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$fileCount files · $totalFormatted cached offline",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                onClick = onNavigateToRadar,
                shape = RoundedCornerShape(12.dp),
                color = if (activePeersCount > 0) Color(0xFF064E3B) else Color(0xFF1E293B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (activePeersCount > 0) MeshSuccess else MeshWarning)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activePeersCount > 0) "$activePeersCount Online" else "Radar (0)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (activePeersCount > 0) MeshSuccess else Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}

@Composable
fun VaultFileCard(
    file: SyncedFile,
    onPreview: () -> Unit,
    onSync: () -> Unit,
    onSimulateConflict: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() }
            .testTag("file_card_${file.id.take(8)}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FileCategoryIcon(category = file.category, size = 42)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = file.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (file.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned in cache",
                                tint = MeshCyan80,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = file.sizeFormatted,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "·",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "v${file.versionNumber} (L:${file.lamportTimestamp})",
                            fontSize = 11.sp,
                            color = MeshIndigo80,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "·",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(Date(file.lastModifiedTimestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SyncStatusBadge(status = file.syncStatus)

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "File menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open & Edit Document") },
                            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onPreview()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sync via Bluetooth Mesh") },
                            leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = MeshCyan80) },
                            onClick = {
                                menuExpanded = false
                                onSync()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Test Conflict Simulation") },
                            leadingIcon = { Icon(Icons.Default.SyncProblem, contentDescription = null, tint = MeshWarning) },
                            onClick = {
                                menuExpanded = false
                                onSimulateConflict()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (file.isPinned) "Unpin from Cache" else "Pin in Cache (Never Evict)") },
                            leadingIcon = { Icon(if (file.isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onTogglePin()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete from Vault", color = MeshDanger) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MeshDanger) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Quick text snippet preview if present
            file.textPreview?.let { preview ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = preview.trim(),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom Action Bar
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSync,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync",
                        modifier = Modifier.size(14.dp),
                        tint = MeshCyan80
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Live", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyVaultView(
    hasSearch: Boolean,
    onAddNote: () -> Unit,
    onImportFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasSearch) "No Matching Files Found" else "Offline Vault is Empty",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (hasSearch) "Try a different search keyword or clear filters" else "Create a note or import documents to start syncing with nearby Bluetooth peers offline.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (!hasSearch) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAddNote,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Note")
                }
                OutlinedButton(
                    onClick = onImportFile,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import File")
                }
            }
        }
    }
}

@Composable
fun PeerPickerDialog(
    peers: List<PeerDevice>,
    fileName: String,
    onDismiss: () -> Unit,
    onSelectPeer: (PeerDevice) -> Unit,
    onNavigateToRadar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Sync '$fileName'", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Select a nearby Bluetooth peer node to stream and synchronize this file:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (peers.isEmpty()) {
                    Text(
                        text = "No Bluetooth peers in range. Open Bluetooth Radar to discover nearby devices or add a test peer.",
                        fontSize = 12.sp,
                        color = MeshWarning
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        peers.forEach { peer ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPeer(peer) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (peer.isConnected) MeshSuccess else Color(0xFF64748B))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(peer.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(peer.deviceModel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        if (peer.isConnected) "Connected" else "Offline",
                                        fontSize = 11.sp,
                                        color = if (peer.isConnected) MeshSuccess else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (peers.isEmpty()) {
                Button(onClick = onNavigateToRadar) {
                    Text("Open Radar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SimulateConflictDialog(
    peers: List<PeerDevice>,
    fileName: String,
    onDismiss: () -> Unit,
    onSelectPeer: (PeerDevice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Simulate Sync Conflict", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Simulate a concurrent conflicting edit on '$fileName' coming from another offline peer node to test vector clock conflict resolution:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                peers.forEach { peer ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectPeer(peer) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SyncProblem, contentDescription = null, tint = MeshWarning)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(peer.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf(".md") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Offline Document", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("File Name") },
                    placeholder = { Text("e.g., project_notes") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // File Type Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(".md", ".txt", ".json", ".kt", ".py").forEach { ext ->
                        FilterChip(
                            selected = extension == ext,
                            onClick = { extension = ext },
                            label = { Text(ext, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Code") },
                    placeholder = { Text("Type content to cache and sync across devices...") },
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanTitle = if (title.isBlank()) "Untitled_${System.currentTimeMillis()}" else title.trim()
                    val fullFileName = if (cleanTitle.contains('.')) cleanTitle else "$cleanTitle$extension"
                    onCreate(fullFileName, content)
                },
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("Save to Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
