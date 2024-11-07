package com.apnamart.geofencing_module.geofencing.event_handler

import com.apnamart.geofencing_module.geofencing.data.TriggeredGeofenceData

interface GeofenceEventHandler {
    suspend fun onGeofenceAdded() {}
    suspend fun onGeofenceEntered(triggeredGeofenceData: TriggeredGeofenceData) {}
    suspend fun onGeofenceExited(triggeredGeofenceData: TriggeredGeofenceData) {}
    suspend fun onGeofenceDwelled(triggeredGeofenceData: TriggeredGeofenceData) {}
    suspend fun onGeofenceError(errorMessage: String) {}
    suspend fun onFailure(exception: Exception) {}
}
