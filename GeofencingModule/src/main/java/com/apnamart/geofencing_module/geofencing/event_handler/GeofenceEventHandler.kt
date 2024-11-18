package com.apnamart.geofencing_module.geofencing.event_handler

import com.apnamart.geofencing_module.geofencing.data.TriggeredGeofenceData

interface GeofenceEventHandler {
    suspend fun onGeofenceAdded() {}
    suspend fun onGeofenceEntered(triggeredGeofenceList: List<TriggeredGeofenceData>) {}
    suspend fun onGeofenceExited(triggeredGeofenceList: List<TriggeredGeofenceData>) {}
    suspend fun onGeofenceDwelled(triggeredGeofenceList: List<TriggeredGeofenceData>) {}
    suspend fun onGeofenceError(errorMessage: String) {}
    suspend fun onFailure(exception: Exception) {}
}
