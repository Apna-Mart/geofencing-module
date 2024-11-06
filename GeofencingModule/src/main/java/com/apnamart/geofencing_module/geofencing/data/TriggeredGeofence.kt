package com.apnamart.geofencing_module.geofencing.data

import android.location.Location

data class TriggeredGeofence(
    val triggeringLocation : Location,
    val triggeringGeofence : List<GeofenceData>
)