package com.apnamart.geofencingmodule.geofencing.provider

import com.apnamart.geofencingmodule.geofencing.data.GeofenceData


interface GeofenceDataProvider {

    /**
     * Method to check if geofence should be added or not.
     * The app will implement this to provide validation checks to determine if geofence should be added or not.
     *
     * @return Boolean which denotes if geofence should be added or not.
     */
    suspend fun shouldAddGeofence() : Boolean

    /**
     * Method to get the list of geofences to be added.
     * The app will implement this to provide custom geofence data.
     *
     * @return List of GeofenceData representing the geofences to be managed.
     */
    suspend fun getGeofenceData(): List<GeofenceData>
}
