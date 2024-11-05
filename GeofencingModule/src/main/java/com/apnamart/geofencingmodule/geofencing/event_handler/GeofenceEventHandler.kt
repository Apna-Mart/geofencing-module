package com.apnamart.geofencingmodule.geofencing.event_handler

import com.apnamart.geofencingmodule.geofencing.data.GeofenceData
import com.apnamart.geofencingmodule.geofencing.data.TriggeredGeofence

interface GeofenceEventHandler {
    fun onGeofenceEntered(triggeredGeofence: TriggeredGeofence) {}
    fun onGeofenceExited(triggeredGeofence: TriggeredGeofence) {}
    fun onGeofenceDwelled(triggeredGeofence: TriggeredGeofence) {}
    fun onGeofenceError(errorMessage: String) {}
}
