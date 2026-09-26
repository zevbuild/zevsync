package com.example

import android.app.Application
import android.content.Context
import android.os.Build
import com.example.data.bluetooth.BluetoothSyncEngine
import com.example.data.cache.CacheVaultManager
import com.example.data.cache.VectorClockEngine
import com.example.data.db.SyncBeamDatabase
import com.example.data.repository.SyncBeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.UUID

class SyncBeamApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { SyncBeamDatabase.getDatabase(this) }
    val vaultManager by lazy { CacheVaultManager(this) }

    val localDeviceId: String by lazy {
        val prefs = getSharedPreferences("syncbeam_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("local_device_id", null)
        if (id == null) {
            id = "node-" + UUID.randomUUID().toString().take(8)
            prefs.edit().putString("local_device_id", id).apply()
        }
        id
    }

    val localDeviceName: String by lazy {
        val prefs = getSharedPreferences("syncbeam_prefs", Context.MODE_PRIVATE)
        prefs.getString("local_device_name", null) ?: "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
    }

    val vectorClockEngine by lazy { VectorClockEngine(localDeviceId) }

    val bluetoothSyncEngine by lazy {
        BluetoothSyncEngine(
            context = this,
            scope = applicationScope,
            vaultManager = vaultManager,
            vectorClockEngine = vectorClockEngine,
            localDeviceId = localDeviceId,
            localDeviceName = localDeviceName
        )
    }

    val repository by lazy {
        SyncBeamRepository(
            context = this,
            scope = applicationScope,
            database = database,
            vaultManager = vaultManager,
            vectorClockEngine = vectorClockEngine,
            bluetoothEngine = bluetoothSyncEngine,
            localDeviceId = localDeviceId,
            localDeviceName = localDeviceName
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
