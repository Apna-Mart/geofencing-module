package com.apnamart.geofencingmodule.geofencing.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TRIGGERING_GEOFENCE
import com.apnamart.geofencingmodule.geofencing.data.GeofenceData
import com.apnamart.geofencingmodule.geofencing.data.TriggeredGeofence
import com.apnamart.geofencingmodule.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencingmodule.geofencing.library.GeofenceModule
import com.apnamart.geofencingmodule.geofencing.permissions.LocationHelper.createLocation
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

        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            coroutineScope.launch {  eventHandler?.onGeofenceError(errorMessage) }
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
            coroutineScope.launch {  handleGeofenceTransition(transitionType ?: 0, triggeredGeofence) }
        }
    }

    private suspend fun handleGeofenceTransition(transitionType: Int, geofenceData: TriggeredGeofence) {
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
