package com.apnamart.geofencingmodule.geofencing.work_manager

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.apnamart.geofencingmodule.geofencing.broadcast_receiver.GeofenceReceiver
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManagerImpl
import com.apnamart.geofencingmodule.geofencing.library.GeofenceLibrary
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper
import com.apnamart.geofencingmodule.geofencing.provider.GeofenceDataProvider
import javax.inject.Inject

class GeofenceWorker (
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        Log.e(GeofenceConstants.TAG, "library worker do work")

        if (!LocationPermissionHelper.checkLocationPermissions(context)) {
            Log.e(GeofenceConstants.TAG, "library worker location permission not found")
            return Result.success()
        }
        ContextCompat.registerReceiver(
            context,
            GeofenceReceiver(),
            IntentFilter(GeofenceConstants.GEO_LOCATION_INTENT_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )


        val geofenceDataList = GeofenceLibrary.getGeofenceDataProvider().getGeofenceData()

        GeofenceManagerImpl(context).removeAndAddGeofences(
            geofenceDataList,
            onSuccess = {
                Log.e(GeofenceConstants.TAG, "geofence added successfully")
            },
            onFailure = {
                Log.e(GeofenceConstants.TAG, "geofence addition failed")
            },
        )

        Log.e(GeofenceConstants.TAG, "library worker success")

        return Result.success()
    }

}

