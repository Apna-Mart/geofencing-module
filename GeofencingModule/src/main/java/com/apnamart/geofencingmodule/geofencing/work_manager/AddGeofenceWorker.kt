package com.apnamart.geofencingmodule.geofencing.work_manager

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apnamart.geofencingmodule.geofencing.broadcast_receiver.GeofenceBroadcastReceiver
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManagerImpl
import com.apnamart.geofencingmodule.geofencing.library.GeofenceLibrary
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper

class AddGeofenceWorker (
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

        val geofenceDataList = GeofenceLibrary.getGeofenceDataProvider().getGeofenceData()

        //need to change this
        GeofenceManagerImpl(context).removeAndAddGeofences(
            geofenceDataList,
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

