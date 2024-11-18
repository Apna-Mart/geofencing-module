package com.apnamart.geofencing_module.geofencing.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants.TRIGGERING_LOCATION
import com.apnamart.geofencing_module.geofencing.data.GeofenceData
import com.apnamart.geofencing_module.geofencing.data.TriggeredGeofenceData
import com.apnamart.geofencing_module.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule
import com.apnamart.geofencing_module.geofencing.permissions.LocationHelper
import com.apnamart.geofencing_module.geofencing.permissions.LocationHelper.createLocation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var eventHandler: GeofenceEventHandler? = null

    override fun onReceive(context: Context, intent: Intent) {
        eventHandler = GeofenceModule.getEventHandler() ?: return
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            GeofenceModule.coroutineScope.launch { eventHandler?.onGeofenceError("no event found") }
            return
        }
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            GeofenceModule.coroutineScope.launch { eventHandler?.onGeofenceError(errorMessage) }
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        val transitionType = geofencingEvent.geofenceTransition

        if (triggeringGeofences.isNullOrEmpty()) {
            GeofenceModule.coroutineScope.launch { eventHandler?.onGeofenceError("no triggering Geofence in geofence event") }
            return
        }

        val triggeringLocation = createLocation(
            TRIGGERING_LOCATION,
            geofencingEvent.triggeringLocation?.latitude ?: 0.0,
            geofencingEvent.triggeringLocation?.longitude ?: 0.0
        )

        GeofenceModule.coroutineScope.launch {
            val triggeredGeofenceList = mutableListOf<TriggeredGeofenceData>()

            val currentLocation =
                LocationHelper.getLocation(context, GeofenceModule.coroutineScope, onError = { e ->
                    GeofenceModule.coroutineScope.launch { eventHandler?.onFailure(e) }
                }) ?: return@launch

            triggeringGeofences.forEach {

                val geofenceData = GeofenceData(
                    requestId = it.requestId,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    radius = it.radius,
                    transitionType = transitionType
                )

                val centerOfGeofenceLocation = createLocation(
                    GeofenceConstants.GEOFENCE_LOCATION,
                    it.latitude,
                    it.longitude
                )

                val geofenceDistanceFromGeofence =
                    triggeringLocation.distanceTo(centerOfGeofenceLocation)
                val currentDistanceFromGeofence =
                    currentLocation.distanceTo(centerOfGeofenceLocation)

                val triggeredGeofenceData = TriggeredGeofenceData(
                    triggeringLocation = triggeringLocation,
                    triggeredGeofence = geofenceData,
                    currentDistanceFromStore = currentDistanceFromGeofence,
                    geofenceDistanceFromStore = geofenceDistanceFromGeofence,
                    currentLocation = currentLocation
                )

                triggeredGeofenceList.add(triggeredGeofenceData)
            }

            handleGeofenceTransition(
                transitionType,
                triggeredGeofenceList
            )
        }
    }

    private suspend fun handleGeofenceTransition(
        transitionType: Int,
        triggeredGeofenceList: List<TriggeredGeofenceData>
    ) {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                val geofenceList =
                    triggeredGeofenceList.filter { triggeredGeofenceData -> triggeredGeofenceData.currentDistanceFromStore <= triggeredGeofenceData.triggeredGeofence.radius }
                        .ifEmpty {
                            return
                        }
                eventHandler?.onGeofenceEntered(geofenceList)
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                val geofenceList =
                    triggeredGeofenceList.filter { triggeredGeofenceData -> triggeredGeofenceData.currentDistanceFromStore > triggeredGeofenceData.triggeredGeofence.radius }
                        .ifEmpty {
                            return
                        }
                eventHandler?.onGeofenceExited(geofenceList)
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                val geofenceList =
                    triggeredGeofenceList.filter { triggeredGeofenceData -> triggeredGeofenceData.currentDistanceFromStore <= triggeredGeofenceData.triggeredGeofence.radius }
                        .ifEmpty {
                            return
                        }
                eventHandler?.onGeofenceDwelled(geofenceList)
            }
            else -> {
                Log.e(TAG, "Unknown geofence transition type: $transitionType")
            }
        }
    }
}
