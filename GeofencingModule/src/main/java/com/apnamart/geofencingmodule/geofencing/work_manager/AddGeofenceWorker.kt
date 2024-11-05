package com.apnamart.geofencingmodule.geofencing.work_manager

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apnamart.geofencingmodule.geofencing.broadcast_receiver.GeofenceBroadcastReceiver
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.library.GeofenceLibrary
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper

class AddGeofenceWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        if (!LocationPermissionHelper.checkLocationPermissions(context)) {
            Log.e(GeofenceConstants.TAG, "location permission not found")
            return Result.success()
        }

        ContextCompat.registerReceiver(
            context,
            GeofenceBroadcastReceiver(),
            IntentFilter(GeofenceConstants.GEO_LOCATION_INTENT_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val geofenceList =
            GeofenceLibrary.getGeofenceDataProvider()?.getGeofenceData() ?: return Result.success()
        val geofenceManager = GeofenceLibrary.getGeofenceManager() ?: return Result.success()

        geofenceManager.removeAndAddGeofences(
            geofenceList,
            onSuccess = {
                Log.e(GeofenceConstants.TAG, "geofence added successfully")
            },
            onFailure = {
                Log.e(GeofenceConstants.TAG, "geofence addition failed")
            },
        )

        return Result.success()
    }

}

