package com.apnamart.geofencingmodule.geofencing.core

import android.app.PendingIntent
import com.apnamart.geofencingmodule.geofencing.data.GeofenceData

/**
 * Interface defining the operations for managing geofences.
 */
interface GeofenceManager {
    /**
     * Adds geofences to the location services.
     *
     * @param geofences List of geofence data to be added.
     * @param pendingIntent PendingIntent that will be triggered when the geofence transitions occur.
     * @param onSuccess Callback invoked when geofences are added successfully.
     * @param onFailure Callback invoked when there is a failure in adding geofences.
     */
    fun addGeofences(
        geofences: List<GeofenceData>,
        pendingIntent: PendingIntent,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    )

    /**
     * Removes all geofences associated with the provided PendingIntent.
     *
     * @param pendingIntent The PendingIntent for which geofences should be removed.
     * @param onSuccess Callback invoked when all geofences are removed successfully.
     */
    fun removeAllGeofences(pendingIntent: PendingIntent, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    /**
     * Removes existing geofences and adds new ones.
     *
     * @param geofences List of geofence data to be added.
     * @param onSuccess Callback invoked when geofences are added successfully.
     * @param onFailure Callback invoked when there is a failure in adding geofences.
     */
    fun removeAndAddGeofences(
        geofences: List<GeofenceData>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    )
}
