package com.apnamart.geofencingmodule.geofencing.work_manager

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TAG
import java.lang.ref.WeakReference


class WorkManagerInitializer(private val context: WeakReference<Context>) {

    val workManager by lazy {
        context.get()?.let {
            try {
                // Attempt to get the WorkManager instance
                WorkManager.getInstance(it)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "WorkManager not initialized: ${e.message}")
                initializeWorkManager(it)
                WorkManager.getInstance(it) // Retrieve WorkManager again after initialization
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
            Log.e(TAG, "Failed to initialize WorkManager: ${e.message}")
        }
    }
}
