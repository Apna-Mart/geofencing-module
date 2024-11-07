package com.apnamart.geofencing_module.geofencing.work_manager

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apnamart.geofencing_module.geofencing.broadcast_receiver.GeofenceBroadcastReceiver
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants
import com.apnamart.geofencing_module.geofencing.data.getGeofenceData
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule.createPendingIntent
import com.apnamart.geofencing_module.geofencing.permissions.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddGeofenceWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun doWork(): Result {

        val geofenceDataProvider = GeofenceModule.getGeofenceDataProvider()
        val geofenceManager = GeofenceModule.getGeofenceManager()
        val geofenceEventHandler = GeofenceModule.getEventHandler() ?: return Result.success()

        if (geofenceDataProvider == null || geofenceManager == null ) {
            geofenceEventHandler.onGeofenceError("geofence data provider or geofence manager not found")
            return Result.success()
        }

        if (!geofenceDataProvider.shouldAddGeofence()) {
            Log.e(GeofenceConstants.TAG, "geofence should not be added")
            return Result.success()
        }


        if (!LocationHelper.checkLocationPermissions(context)) {
            geofenceEventHandler.onGeofenceError("location permission not found")
            Log.e(GeofenceConstants.TAG, "location permission not found")
            return Result.success()
        }

        ContextCompat.registerReceiver(
            context,
            GeofenceBroadcastReceiver(),
            IntentFilter(GeofenceConstants.GEO_LOCATION_INTENT_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val storeGeofenceData = geofenceDataProvider.getStoreGeofenceData()

        val pendingIntent = createPendingIntent(
            context,
            GeofenceBroadcastReceiver::class.java,
            GeofenceConstants.GEO_LOCATION_INTENT_ACTION
        )

        geofenceManager.removeAndAddGeofences(
            getGeofenceData(storeGeofenceData),
            onSuccess = {
                coroutineScope.launch {  geofenceEventHandler.onGeofenceAdded() }
                Log.e(GeofenceConstants.TAG, "geofence added successfully")
            },
            onFailure = { e ->
                coroutineScope.launch {  geofenceEventHandler.onFailure(e)}
                Log.e(GeofenceConstants.TAG, "geofence addition failed")
            },
            pendingIntent
        )

        return Result.success()
    }
}

