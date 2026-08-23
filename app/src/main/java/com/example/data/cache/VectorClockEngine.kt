package com.example.data.cache

import org.json.JSONObject

enum class VectorClockComparison {
    IDENTICAL,
    LOCAL_NEWER,
    REMOTE_NEWER,
    CONCURRENT_CONFLICT
}

class VectorClockEngine(private val localDeviceId: String) {

    fun parseClock(json: String?): Map<String, Long> {
        if (json.isNullOrBlank() || json == "{}") return emptyMap()
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Long>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optLong(key, 0L)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun serializeClock(clock: Map<String, Long>): String {
        val obj = JSONObject()
        for ((device, version) in clock) {
            obj.put(device, version)
        }
        return obj.toString()
    }

    fun incrementLocal(clockJson: String?): Pair<String, Long> {
        val clock = parseClock(clockJson).toMutableMap()
        val currentLocalVersion = clock.getOrDefault(localDeviceId, 0L)
        val newLocalVersion = currentLocalVersion + 1L
        clock[localDeviceId] = newLocalVersion
        return serializeClock(clock) to newLocalVersion
    }

    fun compare(localJson: String?, remoteJson: String?): VectorClockComparison {
        val local = parseClock(localJson)
        val remote = parseClock(remoteJson)

        if (local.isEmpty() && remote.isEmpty()) return VectorClockComparison.IDENTICAL
        if (local.isEmpty()) return VectorClockComparison.REMOTE_NEWER
        if (remote.isEmpty()) return VectorClockComparison.LOCAL_NEWER

        var localHasGreater = false
        var remoteHasGreater = false

        val allKeys = local.keys + remote.keys
        for (key in allKeys) {
            val lVal = local.getOrDefault(key, 0L)
            val rVal = remote.getOrDefault(key, 0L)
            if (lVal > rVal) {
                localHasGreater = true
            } else if (rVal > lVal) {
                remoteHasGreater = true
            }
        }

        return when {
            localHasGreater && !remoteHasGreater -> VectorClockComparison.LOCAL_NEWER
            !localHasGreater && remoteHasGreater -> VectorClockComparison.REMOTE_NEWER
            !localHasGreater && !remoteHasGreater -> VectorClockComparison.IDENTICAL
            else -> VectorClockComparison.CONCURRENT_CONFLICT
        }
    }

    fun merge(localJson: String?, remoteJson: String?): String {
        val local = parseClock(localJson)
        val remote = parseClock(remoteJson)
        val merged = mutableMapOf<String, Long>()

        val allKeys = local.keys + remote.keys
        for (key in allKeys) {
            val lVal = local.getOrDefault(key, 0L)
            val rVal = remote.getOrDefault(key, 0L)
            merged[key] = maxOf(lVal, rVal)
        }
        return serializeClock(merged)
    }

    fun nextLamport(localLamport: Long, remoteLamport: Long = 0L): Long {
        return maxOf(localLamport, remoteLamport) + 1L
    }
}
