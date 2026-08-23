package com.example.data.bluetooth

import org.json.JSONArray
import org.json.JSONObject

data class FileCatalogItem(
    val fileId: String,
    val name: String,
    val relativePath: String,
    val sizeBytes: Long,
    val mimeType: String,
    val contentHash: String,
    val versionNumber: Long,
    val originDeviceId: String,
    val originDeviceName: String,
    val lastModifiedTimestamp: Long,
    val lamportTimestamp: Long,
    val vectorClockJson: String
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("fileId", fileId)
            put("name", name)
            put("relativePath", relativePath)
            put("sizeBytes", sizeBytes)
            put("mimeType", mimeType)
            put("contentHash", contentHash)
            put("versionNumber", versionNumber)
            put("originDeviceId", originDeviceId)
            put("originDeviceName", originDeviceName)
            put("lastModifiedTimestamp", lastModifiedTimestamp)
            put("lamportTimestamp", lamportTimestamp)
            put("vectorClockJson", vectorClockJson)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): FileCatalogItem {
            return FileCatalogItem(
                fileId = json.getString("fileId"),
                name = json.getString("name"),
                relativePath = json.optString("relativePath", ""),
                sizeBytes = json.getLong("sizeBytes"),
                mimeType = json.getString("mimeType"),
                contentHash = json.getString("contentHash"),
                versionNumber = json.optLong("versionNumber", 1L),
                originDeviceId = json.getString("originDeviceId"),
                originDeviceName = json.optString("originDeviceName", "Unknown Peer"),
                lastModifiedTimestamp = json.getLong("lastModifiedTimestamp"),
                lamportTimestamp = json.optLong("lamportTimestamp", 1L),
                vectorClockJson = json.optString("vectorClockJson", "{}")
            )
        }
    }
}

data class HandshakePacket(
    val deviceId: String,
    val deviceName: String,
    val protocolVersion: Int = 2,
    val fileCount: Int,
    val totalVaultBytes: Long
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("type", "HANDSHAKE")
            put("deviceId", deviceId)
            put("deviceName", deviceName)
            put("protocolVersion", protocolVersion)
            put("fileCount", fileCount)
            put("totalVaultBytes", totalVaultBytes)
        }.toString()
    }

    companion object {
        fun fromJson(raw: String): HandshakePacket? {
            return try {
                val json = JSONObject(raw)
                HandshakePacket(
                    deviceId = json.getString("deviceId"),
                    deviceName = json.getString("deviceName"),
                    protocolVersion = json.optInt("protocolVersion", 2),
                    fileCount = json.optInt("fileCount", 0),
                    totalVaultBytes = json.optLong("totalVaultBytes", 0L)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
