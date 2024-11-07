package com.apnamart.geofencing_module.geofencing.work_manager

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class WorkManagerInitializer(private val context: WeakReference<Context>) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    val workManager by lazy {
        context.get()?.let {
            try {
                // Attempt to get the WorkManager instance
                Log.e(TAG, "WorkManager instance already initialised")
                WorkManager.getInstance(it)
            } catch (e: IllegalStateException) {
                coroutineScope.launch {  GeofenceModule.getEventHandler()?.onFailure(e) }
                Log.e(TAG, "WorkManager not initialized: ${e.message}")
                initializeWorkManager(it)
                WorkManager.getInstance(it)
            }
        }
    }

    private fun initializeWorkManager(context: Context) {
        try {
            WorkManager.initialize(
                context,
                Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .build()
            )
            Log.e(TAG, "WorkManager initialized successfully")
        } catch (e: IllegalStateException) {
            coroutineScope.launch {  GeofenceModule.getEventHandler()?.onFailure(e) }
            Log.e(TAG, "Failed to initialize WorkManager: ${e.message}")
        }
    }
}
