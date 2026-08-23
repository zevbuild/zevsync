package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.StorageProgressBar
import com.example.ui.theme.MeshCyan80
import com.example.ui.theme.MeshDanger
import com.example.ui.theme.MeshIndigo80
import com.example.ui.theme.MeshSuccess
import com.example.ui.theme.MeshWarning

@Composable
fun StorageMeshScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 88.dp, start = 16.dp, end = 16.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Storage Quota Overview Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MeshCyan80,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cache Storage Quota", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Text(
                            text = "${formatBytes(uiState.storageBreakdown.totalVaultBytes)} / ${String.format("%.1f GB", uiState.quotaLimitGb)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MeshCyan80
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    StorageProgressBar(breakdown = uiState.storageBreakdown)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Quota limit slider: ${String.format("%.1f GB", uiState.quotaLimitGb)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = uiState.quotaLimitGb,
                        onValueChange = { viewModel.setQuotaLimitGb(it) },
                        valueRange = 0.5f..10.0f,
                        steps = 19,
                        modifier = Modifier.testTag("quota_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showPurgeConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MeshDanger),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("purge_cache_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge Cache Storage")
                        }
                    }
                }
            }
        }

        // Storage Category Breakdown Cards
        item {
            Text("Content Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryStorageRow("Documents & PDFs", uiState.storageBreakdown.docsBytes, Icons.Default.Description, Color(0xFF3B82F6))
                CategoryStorageRow("Code & Data Files", uiState.storageBreakdown.codeBytes, Icons.Default.Code, Color(0xFF10B981))
                CategoryStorageRow("Images & Photos", uiState.storageBreakdown.imagesBytes, Icons.Default.Image, Color(0xFFEC4899))
                CategoryStorageRow("Audio & Voice Memos", uiState.storageBreakdown.audioBytes, Icons.Default.MusicNote, Color(0xFF8B5CF6))
                CategoryStorageRow("Videos & Media", uiState.storageBreakdown.videoBytes, Icons.Default.Movie, Color(0xFFF97316))
                CategoryStorageRow("Archives & Other", uiState.storageBreakdown.archiveBytes + uiState.storageBreakdown.otherBytes, Icons.Default.FolderZip, Color(0xFFEAB308))
            }
        }

        // Offline Bluetooth Mesh Configuration
        item {
            Text("Offline Mesh Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Auto-sync switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Sync on Bluetooth Connect", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Automatically streams catalog diffs when a peer node comes in range.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.autoSyncOnConnect,
                            onCheckedChange = { viewModel.toggleAutoSync(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mesh Relay switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Multi-Hop Mesh Relay", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Relays packets between distant Bluetooth devices through intermediate nodes.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.meshRelayEnabled,
                            onCheckedChange = { viewModel.toggleMeshRelay(it) }
                        )
                    }
                }
            }
        }

        // Local Device Node Identity
        item {
            Text("Local Node Identity", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF312E81)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DeviceHub, contentDescription = null, tint = MeshIndigo80, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Decentralized Node Identity", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("No centralized login needed · Fully offline", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("LAMPORT CLOCK ENGINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MeshCyan80)
                            Text("Active Vector Clock Lineage Tracker", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("SHA-256 CAS Integrity: Enabled", fontSize = 10.sp, color = MeshSuccess)
                        }
                    }
                }
            }
        }
    }

    if (showPurgeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirmDialog = false },
            title = { Text("Purge Unpinned Cache?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will remove downloaded peer cache files that are not pinned to free up storage. Your own files and pinned documents will remain intact.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purgeCache()
                        showPurgeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeshDanger)
                ) {
                    Text("Purge Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CategoryStorageRow(title: String, bytes: Long, icon: ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Text(formatBytes(bytes), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
