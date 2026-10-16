package com.example.data.bluetooth

import java.util.UUID

object BluetoothConstants {
    // Standard RFCOMM SPP UUID and App-specific custom UUID for SyncBeam
    val SYNC_BEAM_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    const val SERVICE_NAME = "SyncBeamMeshService"

    // Packet Type Headers
    const val TYPE_HANDSHAKE = 0x01
    const val TYPE_CATALOG_SYNC_REQ = 0x02
    const val TYPE_CATALOG_SYNC_RESP = 0x03
    const val TYPE_FILE_CHUNK_REQ = 0x04
    const val TYPE_FILE_CHUNK_DATA = 0x05
    const val TYPE_FILE_CHUNK_ACK = 0x06
    const val TYPE_CONFLICT_NOTIFY = 0x07
    const val TYPE_PING_HEARTBEAT = 0x08

    const val CHUNK_SIZE_BYTES = 64 * 1024 // 64 KB per chunk
}
