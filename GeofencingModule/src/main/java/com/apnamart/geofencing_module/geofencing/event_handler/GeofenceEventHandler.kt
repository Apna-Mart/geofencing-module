package com.apnamart.geofencing_module.geofencing.event_handler

import com.apnamart.geofencing_module.geofencing.data.TriggeredGeofence

interface GeofenceEventHandler {
    suspend fun onGeofenceEntered(triggeredGeofence: TriggeredGeofence) {}
    suspend fun onGeofenceExited(triggeredGeofence: TriggeredGeofence) {}
    suspend fun onGeofenceDwelled(triggeredGeofence: TriggeredGeofence) {}
    suspend fun onGeofenceError(errorMessage: String) {}
}
