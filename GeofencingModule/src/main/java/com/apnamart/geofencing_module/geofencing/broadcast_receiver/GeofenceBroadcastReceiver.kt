package com.apnamart.geofencing_module.geofencing.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants.TRIGGERING_GEOFENCE
import com.apnamart.geofencing_module.geofencing.data.GeofenceData
import com.apnamart.geofencing_module.geofencing.data.TriggeredGeofence
import com.apnamart.geofencing_module.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule
import com.apnamart.geofencing_module.geofencing.permissions.LocationHelper
import com.apnamart.geofencing_module.geofencing.permissions.LocationHelper.createLocation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var eventHandler: GeofenceEventHandler? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        eventHandler = GeofenceModule.getEventHandler() ?: return

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            coroutineScope.launch { eventHandler?.onGeofenceError("no event found") }
            return
        }
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            coroutineScope.launch { eventHandler?.onGeofenceError(errorMessage) }
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return
        val transitionType = geofencingEvent.geofenceTransition

        val triggeringLocation = createLocation(
            TRIGGERING_GEOFENCE,
            geofencingEvent.triggeringLocation?.latitude ?: 0.0,
            geofencingEvent.triggeringLocation?.longitude ?: 0.0
        )

        coroutineScope.launch {
            val geofenceList = mutableListOf<GeofenceData>()
            for (geofence in triggeringGeofences) {
                geofenceList.add(
                    GeofenceData(
                        requestId = geofence.requestId,
                        latitude = geofence.latitude,
                        longitude = geofence.longitude,
                        radius = geofence.radius,
                        transitionType = transitionType
                    )
                )
            }

            val location = createLocation(
                GeofenceConstants.GEOFENCE_LOCATION,
                geofenceList.first().latitude,
                geofenceList.first().longitude
            )

            val currentLocation =
                LocationHelper.getLocation(context, coroutineScope, onError = { e ->
                    coroutineScope.launch { eventHandler?.onGeofenceError(e.toString()) }
                }) ?: return@launch


            val geofenceDistanceFromStore = triggeringLocation.distanceTo(location)
            val currentDistanceFromStore = currentLocation.distanceTo(location)

            val triggeredGeofence = TriggeredGeofence(
                triggeringLocation = triggeringLocation,
                triggeringGeofence = geofenceList,
                currentDistanceFromStore = currentDistanceFromStore,
                geofenceDistanceFromStore = geofenceDistanceFromStore,
                currentLocation = currentLocation
            )
            handleGeofenceTransition(
                transitionType,
                triggeredGeofence
            )
        }
    }

    private suspend fun handleGeofenceTransition(
        transitionType: Int,
        geofenceData: TriggeredGeofence
    ) {

        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                if (geofenceData.currentDistanceFromStore <= geofenceData.triggeringGeofence.first().radius) {
                    eventHandler?.onGeofenceEntered(geofenceData)
                }
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                if (geofenceData.currentDistanceFromStore >= geofenceData.triggeringGeofence.first().radius) {
                    eventHandler?.onGeofenceExited(geofenceData)
                }
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                if (geofenceData.currentDistanceFromStore <= geofenceData.triggeringGeofence.first().radius) {
                    eventHandler?.onGeofenceDwelled(geofenceData)
                }
            }
            else -> {
                Log.e(TAG, "Unknown geofence transition type: $transitionType")
            }
        }
    }
}
