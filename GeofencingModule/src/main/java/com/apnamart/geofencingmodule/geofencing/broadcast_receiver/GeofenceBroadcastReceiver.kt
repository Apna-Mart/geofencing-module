package com.apnamart.geofencingmodule.geofencing.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TRIGGERING_GEOFENCE
import com.apnamart.geofencingmodule.geofencing.data.GeofenceData
import com.apnamart.geofencingmodule.geofencing.data.TriggeredGeofence
import com.apnamart.geofencingmodule.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencingmodule.geofencing.library.GeofenceLibrary
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper.createLocation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private var eventHandler: GeofenceEventHandler? = null

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "onReceiver ", Toast.LENGTH_LONG).show()
        eventHandler = GeofenceLibrary.getEventHandler() ?: return eventHandler?.onGeofenceError("event handler is null") ?: return

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            eventHandler?.onGeofenceError(errorMessage)
            return
        }

        val triggeringGeofences = geofencingEvent?.triggeringGeofences
        val transitionType = geofencingEvent?.geofenceTransition

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
                    TRIGGERING_GEOFENCE,
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
                eventHandler?.onGeofenceEntered(geofenceData)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                eventHandler?.onGeofenceExited(geofenceData)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                eventHandler?.onGeofenceDwelled(geofenceData)
            }
            else -> {
                Log.e(TAG, "Unknown geofence transition type: $transitionType")
            }
        }
    }

}
