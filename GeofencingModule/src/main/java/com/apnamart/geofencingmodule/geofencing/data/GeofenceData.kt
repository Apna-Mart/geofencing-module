package com.apnamart.geofencingmodule.geofencing.data

import com.google.android.gms.location.Geofence

/**
 * Data class representing the properties required to create a geofence.
 *
 * @property requestId Unique identifier for the geofence.
 * @property latitude Latitude of the geofence center.
 * @property longitude Longitude of the geofence center.
 * @property radius Radius (in meters) of the geofence.
 * @property transitionType Transition types for the geofence (enter, exit, dwell).
 * @property delay Optional loitering delay in milliseconds.
 */
data class GeofenceData(
    val requestId: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val transitionType: Int = Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL,
    val delay: Int? = null
) {
    /**
     * Converts GeofenceData to a Geofence object.
     *
     * @param onFailure Callback invoked when there is a failure in creating a Geofence.
     * @return A Geofence object or null if an exception occurred.
     */
    fun toGeofence(onFailure: (Exception) -> Unit): Geofence? {
        return try {
            // Create a Geofence object using the provided properties
            Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(latitude, longitude, radius)
                .setTransitionTypes(transitionType)
                .apply {
                    delay?.let { setLoiteringDelay(it) } // Set loitering delay if provided
                }
                .build()
        } catch (e: Exception) {
            onFailure.invoke(e)
            null
        }
    }
}


