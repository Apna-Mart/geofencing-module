package com.apnamart.geofencing_module.geofencing.data

import android.location.Location

data class TriggeredGeofenceData(
    val triggeringLocation : Location,
    val triggeredGeofence : GeofenceData,
    val geofenceDistanceFromDestination : Float,
    val currentDistanceFromDestination : Float,
    val currentLocation : Location
)
