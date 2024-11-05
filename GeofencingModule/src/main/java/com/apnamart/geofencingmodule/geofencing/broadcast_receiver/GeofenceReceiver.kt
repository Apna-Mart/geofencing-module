package com.apnamart.geofencingmodule.geofencing.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencingmodule.geofencing.data.GeofenceData
import com.apnamart.geofencingmodule.geofencing.data.TriggeredGeofence
import com.apnamart.geofencingmodule.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencingmodule.geofencing.library.GeofenceLibrary
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper.createLocation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import javax.inject.Inject

public class GeofenceReceiver() : BroadcastReceiver() {

    lateinit var eventHandler: GeofenceEventHandler

    override fun onReceive(context: Context, intent: Intent) { // Handle the received broadcast

        eventHandler = GeofenceLibrary.getEventHandler()
        Log.e(GeofenceConstants.TAG, "library onReceive")

        Toast.makeText(context, "library onReceive", Toast.LENGTH_LONG).show()

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent?.hasError() == true) {
            // Handle error
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            eventHandler.onGeofenceError(errorMessage)
            Log.e(GeofenceConstants.TAG, "library error message $errorMessage")
            return
        }

        // Get the triggering geofence IDs
        val triggeringGeofences = geofencingEvent?.triggeringGeofences
        val transitionType = geofencingEvent?.geofenceTransition

        Log.e(GeofenceConstants.TAG, "library geofence triggered  $triggeringGeofences $transitionType")

        triggeringGeofences?.let {
            val geofenceList = mutableListOf<GeofenceData>()
            for (geofence in triggeringGeofences) {
               geofenceList.add(GeofenceData(
                    requestId = geofence.requestId,
                    latitude = geofence.latitude,
                    longitude = geofence.longitude,
                    radius = geofence.radius,
                    transitionType = transitionType ?: 0
                )
               )
            }
            val triggeredGeofence =  TriggeredGeofence(
                triggeringLocation = createLocation(
                    "triggering_geofence",
                    geofencingEvent.triggeringLocation?.latitude ?: 0.0,
                geofencingEvent.triggeringLocation?.longitude ?: 0.0
            ),
                triggeringGeofence = geofenceList
            )
            handleGeofenceTransition(transitionType ?: 0, triggeredGeofence)
        }
    }

    private fun handleGeofenceTransition(transitionType: Int, geofenceData: TriggeredGeofence) {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                eventHandler.onGeofenceEntered(geofenceData)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                eventHandler.onGeofenceExited(geofenceData)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                eventHandler.onGeofenceDwelled(geofenceData)
            }
            else -> {
                Log.e(TAG, "Unknown geofence transition type: $transitionType")
            }
        }
    }

}
