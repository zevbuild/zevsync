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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PeerDevice
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.BluetoothRadarAnimation
import com.example.ui.theme.MeshCyan80
import com.example.ui.theme.MeshDanger
import com.example.ui.theme.MeshIndigo80
import com.example.ui.theme.MeshSuccess
import com.example.ui.theme.MeshWarning

@Composable
fun BluetoothRadarScreen(
    uiState: UiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showAddPeerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Bluetooth Radar Hero Section
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = MeshCyan80,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bluetooth RFCOMM Mesh",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (uiState.isScanning) "Actively scanning nearby nodes..." else "Listener active on UUID fa87c0d0",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (uiState.isServerListening) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                    ) {
                        Text(
                            text = if (uiState.isServerListening) "SERVER READY" else "OFFLINE",
                            color = if (uiState.isServerListening) MeshSuccess else MeshDanger,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Radar Visualization
                BluetoothRadarAnimation(
                    isScanning = uiState.isScanning || uiState.discoveredPeers.any { it.isConnected },
                    connectedCount = uiState.discoveredPeers.count { it.isConnected }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scan & Peer Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (uiState.isScanning) viewModel.stopBluetoothScan() else viewModel.startBluetoothScan()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isScanning) MeshWarning else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bluetooth_scan_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isScanning) Icons.Default.Refresh else Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (uiState.isScanning) "Stop Scan" else "Scan Radar")
                    }

                    OutlinedButton(
                        onClick = { showAddPeerDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_virtual_peer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Node")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Peer Devices Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nearby Nodes (${uiState.discoveredPeers.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.discoveredPeers.count { it.isConnected }} connected",
                fontSize = 12.sp,
                color = MeshCyan80
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Peer Device Cards List
        if (uiState.discoveredPeers.isEmpty()) {
            EmptyPeersView(onScan = { viewModel.startBluetoothScan() })
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.discoveredPeers, key = { it.id }) { peer ->
                    PeerDeviceCard(
                        peer = peer,
                        onToggleConnect = { viewModel.togglePeerConnection(peer.id) },
                        onSyncAll = { viewModel.syncAllFilesWithPeer(peer) }
                    )
                }
            }
        }
    }

    if (showAddPeerDialog) {
        AddVirtualPeerDialog(
            onDismiss = { showAddPeerDialog = false },
            onAdd = { name, model ->
                viewModel.addVirtualMeshPeer(name, model)
                showAddPeerDialog = false
            }
        )
    }
}

@Composable
fun PeerDeviceCard(
    peer: PeerDevice,
    onToggleConnect: () -> Unit,
    onSyncAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("peer_card_${peer.id.take(8)}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Device Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (peer.isConnected) Brush.linearGradient(listOf(Color(0xFF00D2BA), Color(0xFF006A60)))
                            else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (peer.isVirtualNode) Icons.Default.Hub else Icons.Default.Smartphone,
                        contentDescription = "Device",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = peer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (peer.isVirtualNode) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF312E81)
                            ) {
                                Text(
                                    text = "SIMULATOR",
                                    fontSize = 9.sp,
                                    color = MeshIndigo80,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${peer.deviceModel} · RSSI: ${peer.rssi} dBm",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Connect / Disconnect Toggle
                IconButton(
                    onClick = onToggleConnect,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (peer.isConnected) Icons.Default.Link else Icons.Default.LinkOff,
                        contentDescription = "Toggle connection",
                        tint = if (peer.isConnected) MeshSuccess else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Data throughput & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "↑ ${formatBytes(peer.totalBytesSent)}",
                        fontSize = 11.sp,
                        color = MeshCyan80
                    )
                    Text(
                        text = "↓ ${formatBytes(peer.totalBytesReceived)}",
                        fontSize = 11.sp,
                        color = MeshIndigo80
                    )
                    Text(
                        text = "Hop: ${peer.meshHopCount}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onSyncAll,
                    enabled = peer.isConnected,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Vault", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyPeersView(onScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BluetoothSearching,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Bluetooth Peers Discovered",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Make sure Bluetooth is enabled on nearby devices, or add a virtual test node.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun AddVirtualPeerDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("Pixel 9 / Tablet Node") }

    val presetDevices = listOf(
        "MacBook / Linux Bridge" to "Laptop Peer",
        "Galaxy Tab S10" to "Tablet Node",
        "Pixel 9 Pro XL" to "Mobile Mesh",
        "Raspberry Pi Mesh Router" to "Home Vault Node"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Mesh Peer Node", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add a virtual or paired Bluetooth peer node to test continuous offline sync:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Peer Device Name") },
                    placeholder = { Text("e.g., Pixel 9 (Study Room)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Or choose a preset template:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                presetDevices.forEach { (presetName, presetModel) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                name = presetName
                                model = presetModel
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp), tint = MeshCyan80)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(presetName, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = if (name.isBlank()) "Nearby Node (${(100..999).random()})" else name.trim()
                    onAdd(cleanName, model)
                }
            ) {
                Text("Join Mesh")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
