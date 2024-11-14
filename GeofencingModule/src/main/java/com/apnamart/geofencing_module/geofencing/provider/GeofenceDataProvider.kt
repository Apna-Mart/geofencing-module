package com.apnamart.geofencing_module.geofencing.provider

import com.apnamart.geofencing_module.geofencing.data.GeofenceData
import com.apnamart.geofencing_module.geofencing.data.StoreGeofenceData
import com.google.android.gms.location.Geofence


interface GeofenceDataProvider {

    /**
     * Method to check if geofence should be added or not.
     * The app will implement this to provide validation checks to determine if geofence should be added or not.
     *
     * @return Boolean which denotes if geofence should be added or not.
     */
    suspend fun shouldAddGeofence() : Boolean

    suspend fun getStoreGeofenceData(): StoreGeofenceData

    fun getTemporaryGeofenceDisableTimeInHours(): Long
}
