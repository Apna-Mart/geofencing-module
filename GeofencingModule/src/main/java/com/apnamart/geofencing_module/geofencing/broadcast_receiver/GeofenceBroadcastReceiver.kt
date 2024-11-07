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

        if (triggeringGeofences == null){
             GeofenceModule.coroutineScope.launch { eventHandler?.onGeofenceError("no triggering Geofence in geofence event") }
            return
        }

        //TODO : this has support of only catering to a single geofence, and
        // assuming it as the point of truth, as we are only providing a single geofence's data, bt need to add a support of multiple geofences with different lat long later
        val triggeringLocation = createLocation(
            TRIGGERING_GEOFENCE,
            geofencingEvent.triggeringLocation?.latitude ?: 0.0,
            geofencingEvent.triggeringLocation?.longitude ?: 0.0
        )

         GeofenceModule.coroutineScope.launch {
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
                LocationHelper.getLocation(context,  GeofenceModule.coroutineScope, onError = { e ->
                     GeofenceModule.coroutineScope.launch { eventHandler?.onFailure(e) }
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
