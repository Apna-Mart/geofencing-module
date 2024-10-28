package com.apnamart.geofencingmodule.geofencing.geofence_entity

data class GeofenceData(
    val requestId: String,
    val radius: Float,
    val latitude: Double,
    val longitude: Double,
    val delay: Int? = null,
    val transactionType: Int,
)
