package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConflictStatus
import com.example.data.model.SyncConflict
import com.example.data.repository.ConflictResolutionChoice
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.MeshCyan80
import com.example.ui.theme.MeshDanger
import com.example.ui.theme.MeshIndigo80
import com.example.ui.theme.MeshSuccess
import com.example.ui.theme.MeshWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConflictsScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var conflictToResolve by remember { mutableStateOf<SyncConflict?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Status Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.pendingConflicts.isNotEmpty()) Color(0xFF451A03) else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (uiState.pendingConflicts.isNotEmpty()) MeshWarning else MeshSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.pendingConflicts.isNotEmpty()) Icons.Default.SyncProblem else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (uiState.pendingConflicts.isNotEmpty()) "${uiState.pendingConflicts.size} Conflict(s) Require Resolution" else "All Files Synchronized",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (uiState.pendingConflicts.isNotEmpty()) MeshWarning else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Vector clocks & Lamport timestamps track lineage across disconnected devices.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active (${uiState.pendingConflicts.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Resolved History (${uiState.resolvedConflicts.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            if (uiState.pendingConflicts.isEmpty()) {
                EmptyConflictsView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.pendingConflicts, key = { it.id }) { conflict ->
                        ConflictItemCard(
                            conflict = conflict,
                            onReview = { conflictToResolve = conflict }
                        )
                    }
                }
            }
        } else {
            if (uiState.resolvedConflicts.isEmpty()) {
                EmptyResolvedHistoryView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.resolvedConflicts, key = { it.id }) { conflict ->
                        ResolvedConflictCard(conflict = conflict)
                    }
                }
            }
        }
    }

    conflictToResolve?.let { conflict ->
        ConflictResolutionDialog(
            conflict = conflict,
            onDismiss = { conflictToResolve = null },
            onResolve = { choice, mergedText ->
                viewModel.resolveConflict(conflict.id, choice, mergedText)
                conflictToResolve = null
            }
        )
    }
}

@Composable
fun ConflictItemCard(
    conflict: SyncConflict,
    onReview: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("conflict_card_${conflict.id.take(8)}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF78350F)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncProblem,
                        contentDescription = "Conflict",
                        tint = MeshWarning,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = conflict.fileName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Concurrent branch from ${conflict.remoteDeviceName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comparison Metrics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("LOCAL DEVICE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MeshCyan80)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Version: v${conflict.localVersion}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Lamport: L:${conflict.localLamport}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateFormat.format(Date(conflict.localTimestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text("REMOTE PEER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MeshIndigo80)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Version: v${conflict.remoteVersion}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Lamport: L:${conflict.remoteLamport}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateFormat.format(Date(conflict.remoteTimestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onReview,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resolve_conflict_button")
            ) {
                Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Resolve Conflict (Side-by-Side)")
            }
        }
    }
}

@Composable
fun ConflictResolutionDialog(
    conflict: SyncConflict,
    onDismiss: () -> Unit,
    onResolve: (ConflictResolutionChoice, String?) -> Unit
) {
    var isManualMergeMode by remember { mutableStateOf(false) }
    var manualMergedText by remember {
        mutableStateOf(
            "=== LOCAL VERSION ===\n${conflict.localTextSnippet ?: ""}\n\n=== REMOTE VERSION (${conflict.remoteDeviceName}) ===\n${conflict.remoteTextSnippet ?: ""}"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CallMerge, contentDescription = null, tint = MeshWarning)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Resolve: ${conflict.fileName}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (!isManualMergeMode) {
                    Text(
                        text = "Choose how to merge storage state across your offline Bluetooth devices:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Side-by-side snippet preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Local Snippet", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MeshCyan80)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = conflict.localTextSnippet ?: "(binary data)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(conflict.remoteDeviceName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MeshIndigo80)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = conflict.remoteTextSnippet ?: "(binary data)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Conflict Resolution Action Options
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onResolve(ConflictResolutionChoice.KEEP_LOCAL_WINNER, null) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("1. Keep Local Version (Mine)", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onResolve(ConflictResolutionChoice.KEEP_REMOTE_WINNER, null) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("2. Adopt Remote Copy (${conflict.remoteDeviceName})", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onResolve(ConflictResolutionChoice.KEEP_BOTH, null) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3. Keep Both (Fork Duplicate Branch)", fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = { isManualMergeMode = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("4. Interactive Manual Merge Editor", fontSize = 12.sp, color = MeshCyan80)
                        }
                    }
                } else {
                    // Manual merge editor
                    Text("Edit unified text to resolve both branches:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = manualMergedText,
                        onValueChange = { manualMergedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onResolve(ConflictResolutionChoice.MERGE_CUSTOM, manualMergedText) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Apply Merged State")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ResolvedConflictCard(conflict: SyncConflict) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = MeshSuccess, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(conflict.fileName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(
                    conflict.resolutionNote ?: "Resolved",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            conflict.resolutionTimestamp?.let {
                Text(dateFormat.format(Date(it)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyConflictsView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MeshSuccess,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("No Active Sync Conflicts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Your offline storage cache is 100% consistent across every connected Bluetooth device.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun EmptyResolvedHistoryView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No Resolved History Yet", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
