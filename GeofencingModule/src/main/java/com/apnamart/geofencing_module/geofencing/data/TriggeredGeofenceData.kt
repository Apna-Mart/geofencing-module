package com.apnamart.geofencing_module.geofencing.data

import android.location.Location

data class TriggeredGeofenceData(
    val triggeringLocation : Location,
    val triggeredGeofence : GeofenceData,
    val geofenceDistanceFromStore : Float,
    val currentDistanceFromStore : Float,
    val currentLocation : Location
)
