package com.apnamart.geofencing_module.geofencing.work_manager

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apnamart.geofencing_module.geofencing.broadcast_receiver.GeofenceBroadcastReceiver
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule.createPendingIntent
import com.apnamart.geofencing_module.geofencing.permissions.LocationHelper

class AddGeofenceWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val geofenceDataProvider = GeofenceModule.getGeofenceDataProvider()
        val geofenceManager = GeofenceModule.getGeofenceManager()

        if (geofenceDataProvider == null || geofenceManager == null) {
            return Result.failure()
        }

        if (!geofenceDataProvider.shouldAddGeofence()) {
            Log.e(GeofenceConstants.TAG, "geofence should not be added")
            return Result.success()
        }


        if (!LocationHelper.checkLocationPermissions(context)) {
            Log.e(GeofenceConstants.TAG, "location permission not found")
            return Result.success()
        }

        ContextCompat.registerReceiver(
            context,
            GeofenceBroadcastReceiver(),
            IntentFilter(GeofenceConstants.GEO_LOCATION_INTENT_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val geofenceList = geofenceDataProvider.getGeofenceData()

        val pendingIntent = createPendingIntent(context, GeofenceBroadcastReceiver::class.java, GeofenceConstants.GEO_LOCATION_INTENT_ACTION)

        geofenceManager.removeAndAddGeofences(
            geofenceList,
            onSuccess = {
                Log.e(GeofenceConstants.TAG, "geofence added successfully")
            },
            onFailure = {
                Log.e(GeofenceConstants.TAG, "geofence addition failed")
            },
            pendingIntent
        )

        return Result.success()
    }
}

