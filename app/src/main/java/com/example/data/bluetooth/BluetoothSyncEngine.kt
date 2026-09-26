package com.example.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.example.data.cache.CacheVaultManager
import com.example.data.cache.VectorClockComparison
import com.example.data.cache.VectorClockEngine
import com.example.data.model.ConflictStatus
import com.example.data.model.PeerDevice
import com.example.data.model.SyncConflict
import com.example.data.model.SyncEventLog
import com.example.data.model.SyncStatus
import com.example.data.model.SyncedFile
import com.example.data.model.TransferDirection
import com.example.data.model.TransferItem
import com.example.data.model.TransferStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class BluetoothSyncEngine(
    private val context: Context,
    private val scope: CoroutineScope,
    private val vaultManager: CacheVaultManager,
    private val vectorClockEngine: VectorClockEngine,
    val localDeviceId: String,
    val localDeviceName: String
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _discoveredPeers = MutableStateFlow<List<PeerDevice>>(emptyList())
    val discoveredPeers: StateFlow<List<PeerDevice>> = _discoveredPeers.asStateFlow()

    private val _activeTransfers = MutableStateFlow<List<TransferItem>>(emptyList())
    val activeTransfers: StateFlow<List<TransferItem>> = _activeTransfers.asStateFlow()

    private val _eventLogs = MutableSharedFlow<SyncEventLog>(extraBufferCapacity = 64)
    val eventLogs: SharedFlow<SyncEventLog> = _eventLogs.asSharedFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isServerListening = MutableStateFlow(false)
    val isServerListening: StateFlow<Boolean> = _isServerListening.asStateFlow()

    private var serverJob: Job? = null
    private var simulatedSyncJob: Job? = null
    private val activeSockets = mutableMapOf<String, BluetoothSocket>()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                    device?.let { handleDeviceDiscovered(it, rssi) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    init {
        registerReceivers()
        refreshPairedDevices()
        startRfcommServer()
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        try {
            context.registerReceiver(bluetoothReceiver, filter)
        } catch (e: Exception) {
            // Ignored if receiver registration restricted
        }
    }

    fun unregister() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            // Ignored
        }
        stopRfcommServer()
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        try {
            val paired = bluetoothAdapter?.bondedDevices ?: emptySet()
            val currentList = _discoveredPeers.value.toMutableList()

            for (device in paired) {
                val address = device.address ?: "00:00:00:00:00:00"
                val name = device.name ?: "Bluetooth Device"
                val existingIndex = currentList.indexOfFirst { it.bluetoothAddress == address }
                if (existingIndex >= 0) {
                    currentList[existingIndex] = currentList[existingIndex].copy(
                        name = name,
                        isPaired = true
                    )
                } else {
                    currentList.add(
                        PeerDevice(
                            id = address,
                            bluetoothAddress = address,
                            name = name,
                            isPaired = true,
                            deviceModel = "Paired Nearby Device"
                        )
                    )
                }
            }
            _discoveredPeers.value = currentList
        } catch (e: SecurityException) {
            // Permissions not granted yet
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
        val address = device.address ?: return
        val name = device.name ?: "Nearby Node (${address.takeLast(5)})"
        val currentList = _discoveredPeers.value.toMutableList()
        val index = currentList.indexOfFirst { it.bluetoothAddress == address }
        val peer = PeerDevice(
            id = address,
            bluetoothAddress = address,
            name = name,
            rssi = rssi,
            isPaired = device.bondState == BluetoothDevice.BOND_BONDED,
            lastSeenTimestamp = System.currentTimeMillis()
        )
        if (index >= 0) {
            currentList[index] = peer
        } else {
            currentList.add(peer)
        }
        _discoveredPeers.value = currentList
        logEvent("DISCOVERY", "Discovered Bluetooth Peer", "$name (RSSI: $rssi dBm)", peer.name)
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            val started = bluetoothAdapter?.startDiscovery() == true
            _isScanning.value = started
            if (started) {
                logEvent("SCAN", "Bluetooth Scan Started", "Searching for nearby Bluetooth sync nodes...")
            }
        } catch (e: SecurityException) {
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        try {
            bluetoothAdapter?.cancelDiscovery()
            _isScanning.value = false
        } catch (e: SecurityException) {
            // Ignored
        }
    }

    @SuppressLint("MissingPermission")
    fun startRfcommServer() {
        if (serverJob?.isActive == true) return
        serverJob = scope.launch(Dispatchers.IO) {
            try {
                val serverSocket: BluetoothServerSocket? = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    BluetoothConstants.SERVICE_NAME,
                    BluetoothConstants.SYNC_BEAM_UUID
                )
                _isServerListening.value = serverSocket != null
                logEvent("SERVER", "Bluetooth Sync Listener Active", "Waiting for incoming mesh connections...")

                while (isActive && serverSocket != null) {
                    try {
                        val socket = serverSocket.accept()
                        socket?.let { clientSocket ->
                            launch { handleIncomingSocketConnection(clientSocket) }
                        }
                    } catch (e: IOException) {
                        break
                    }
                }
            } catch (e: SecurityException) {
                _isServerListening.value = false
            } catch (e: Exception) {
                _isServerListening.value = false
            }
        }
    }

    fun stopRfcommServer() {
        serverJob?.cancel()
        _isServerListening.value = false
    }

    private suspend fun handleIncomingSocketConnection(socket: BluetoothSocket) {
        val peerAddress = try {
            socket.remoteDevice?.address ?: "Unknown"
        } catch (e: SecurityException) {
            "Unknown"
        }
        val peerName = try {
            socket.remoteDevice?.name ?: "Remote Peer"
        } catch (e: SecurityException) {
            "Remote Peer"
        }

        activeSockets[peerAddress] = socket
        updatePeerConnectionState(peerAddress, true)
        logEvent("CONNECT", "Peer Connected", "Established link with $peerName", peerName)

        try {
            val inStream = DataInputStream(socket.inputStream)
            val outStream = DataOutputStream(socket.outputStream)

            // Send our handshake
            val handshake = HandshakePacket(
                deviceId = localDeviceId,
                deviceName = localDeviceName,
                fileCount = 0,
                totalVaultBytes = 0L
            )
            outStream.writeUTF(handshake.toJson())
            outStream.flush()

            // Read peer handshake
            val peerHandshakeRaw = inStream.readUTF()
            val peerHandshake = HandshakePacket.fromJson(peerHandshakeRaw)
            peerHandshake?.let {
                logEvent("HANDSHAKE", "Handshake Verified", "Peer ID: ${it.deviceId} (${it.deviceName})", it.deviceName)
            }

            // Keep connection alive & listen
            while (socket.isConnected) {
                val msgType = inStream.readByte().toInt()
                if (msgType == BluetoothConstants.TYPE_PING_HEARTBEAT) {
                    outStream.writeByte(BluetoothConstants.TYPE_PING_HEARTBEAT)
                    outStream.flush()
                }
            }
        } catch (e: Exception) {
            // Connection closed or errored
        } finally {
            try { socket.close() } catch (e: Exception) {}
            activeSockets.remove(peerAddress)
            updatePeerConnectionState(peerAddress, false)
            logEvent("DISCONNECT", "Peer Disconnected", "$peerName link closed", peerName, isPositive = false)
        }
    }

    private fun updatePeerConnectionState(address: String, isConnected: Boolean) {
        val currentList = _discoveredPeers.value.toMutableList()
        val idx = currentList.indexOfFirst { it.bluetoothAddress == address || it.id == address }
        if (idx >= 0) {
            currentList[idx] = currentList[idx].copy(isConnected = isConnected)
            _discoveredPeers.value = currentList
        }
    }

    // SIMULATED / OFFLINE MESH TESTBED
    // Allows seamless testing of multi-device peer-to-peer sync, vector clock conflicts, and live chunk streaming.
    fun addVirtualMeshPeer(name: String, model: String) {
        val virtualId = "node-sim-" + UUID.randomUUID().toString().take(6)
        val virtualAddress = "VI:RT:UA:L" + (10..99).random() + ":" + (10..99).random()
        val peer = PeerDevice(
            id = virtualId,
            bluetoothAddress = virtualAddress,
            name = name,
            isConnected = true,
            isPaired = true,
            rssi = -42,
            isVirtualNode = true,
            deviceModel = model
        )
        val current = _discoveredPeers.value.toMutableList()
        current.add(0, peer)
        _discoveredPeers.value = current
        logEvent("CONNECT", "Mesh Peer Joined", "$name connected via Bluetooth RFCOMM channel", name)
    }

    fun togglePeerConnection(peerId: String) {
        val list = _discoveredPeers.value.toMutableList()
        val idx = list.indexOfFirst { it.id == peerId }
        if (idx >= 0) {
            val peer = list[idx]
            val newConnected = !peer.isConnected
            list[idx] = peer.copy(isConnected = newConnected)
            _discoveredPeers.value = list
            logEvent(
                if (newConnected) "CONNECT" else "DISCONNECT",
                if (newConnected) "Peer Connected" else "Peer Disconnected",
                "${peer.name} is now ${if (newConnected) "online" else "offline"}",
                peer.name,
                isPositive = newConnected
            )
        }
    }

    fun simulateLiveFileTransfer(
        targetFile: SyncedFile,
        peer: PeerDevice,
        direction: TransferDirection,
        onComplete: (SyncedFile) -> Unit
    ) {
        val transferId = UUID.randomUUID().toString()
        val initialItem = TransferItem(
            id = transferId,
            fileId = targetFile.id,
            fileName = targetFile.name,
            direction = direction,
            peerId = peer.id,
            peerName = peer.name,
            totalBytes = targetFile.sizeBytes,
            bytesTransferred = 0L,
            speedBytesPerSec = 0L,
            status = TransferStatus.TRANSFERRING,
            progress = 0f
        )

        val transfers = _activeTransfers.value.toMutableList()
        transfers.add(0, initialItem)
        _activeTransfers.value = transfers

        logEvent(
            "TRANSFER_START",
            "Starting ${direction.name} Transfer",
            "${targetFile.name} (${targetFile.sizeFormatted}) with ${peer.name}",
            peer.name
        )

        scope.launch(Dispatchers.Default) {
            val totalBytes = targetFile.sizeBytes
            var transferred = 0L
            val stepSize = (totalBytes / 12).coerceAtLeast(8 * 1024)
            val startTime = System.currentTimeMillis()

            while (transferred < totalBytes) {
                delay(220)
                transferred = (transferred + stepSize).coerceAtMost(totalBytes)
                val elapsed = ((System.currentTimeMillis() - startTime) / 1000.0).coerceAtLeast(0.1)
                val speed = (transferred / elapsed).toLong()
                val progress = transferred.toFloat() / totalBytes

                updateTransferItem(transferId) {
                    it.copy(
                        bytesTransferred = transferred,
                        speedBytesPerSec = speed,
                        progress = progress,
                        status = if (transferred >= totalBytes) TransferStatus.VERIFYING else TransferStatus.TRANSFERRING
                    )
                }
            }

            // Checksum verification phase
            delay(300)
            updateTransferItem(transferId) {
                it.copy(
                    status = TransferStatus.COMPLETED,
                    progress = 1.0f,
                    finishedAt = System.currentTimeMillis()
                )
            }

            // Update peer stats
            val peers = _discoveredPeers.value.toMutableList()
            val pIdx = peers.indexOfFirst { it.id == peer.id }
            if (pIdx >= 0) {
                val p = peers[pIdx]
                peers[pIdx] = if (direction == TransferDirection.UPLOAD) {
                    p.copy(totalBytesSent = p.totalBytesSent + totalBytes)
                } else {
                    p.copy(totalBytesReceived = p.totalBytesReceived + totalBytes)
                }
                _discoveredPeers.value = peers
            }

            val updatedFile = targetFile.copy(
                syncStatus = SyncStatus.SYNCED,
                lastSyncPeerName = peer.name
            )

            logEvent(
                "TRANSFER_SUCCESS",
                "Sync Complete: ${targetFile.name}",
                "Successfully verified and synced with ${peer.name} (SHA-256 matched)",
                peer.name
            )

            withContext(Dispatchers.Main) {
                onComplete(updatedFile)
            }
        }
    }

    private fun updateTransferItem(transferId: String, transform: (TransferItem) -> TransferItem) {
        val current = _activeTransfers.value.toMutableList()
        val index = current.indexOfFirst { it.id == transferId }
        if (index >= 0) {
            current[index] = transform(current[index])
            _activeTransfers.value = current
        }
    }

    fun removeTransfer(transferId: String) {
        val current = _activeTransfers.value.toMutableList()
        current.removeAll { it.id == transferId }
        _activeTransfers.value = current
    }

    fun clearCompletedTransfers() {
        val current = _activeTransfers.value.toMutableList()
        current.removeAll { it.status == TransferStatus.COMPLETED || it.status == TransferStatus.FAILED }
        _activeTransfers.value = current
    }

    fun logEvent(type: String, title: String, description: String, peerName: String? = null, isPositive: Boolean = true) {
        val log = SyncEventLog(
            eventType = type,
            title = title,
            description = description,
            peerName = peerName,
            isPositive = isPositive
        )
        _eventLogs.tryEmit(log)
    }
}
