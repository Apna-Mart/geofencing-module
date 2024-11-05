package com.apnamart.geofencingmodule.geofencing.provider

import com.apnamart.geofencingmodule.geofencing.data.GeofenceData


interface GeofenceDataProvider {
    /**
     * Method to get the list of geofences to be added.
     * The app will implement this to provide custom geofence data.
     *
     * @return List of GeofenceData representing the geofences to be managed.
     */
    fun getGeofenceData(): List<GeofenceData>
}
