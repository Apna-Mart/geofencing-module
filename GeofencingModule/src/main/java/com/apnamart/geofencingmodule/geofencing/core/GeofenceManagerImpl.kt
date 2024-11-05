package com.apnamart.geofencingmodule.geofencing.core


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.apnamart.geofencingmodule.geofencing.broadcast_receiver.GeofenceBroadcastReceiver
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencingmodule.geofencing.data.GeofenceData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/**
 * Implementation of the GeofenceManager interface.
 * This class handles the addition, removal, and management of geofences.
 */
class GeofenceManagerImpl(private val context: Context) : GeofenceManager {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    @SuppressLint("MissingPermission")
    override fun addGeofences(
        geofences: List<GeofenceData>,
        pendingIntent: PendingIntent,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
            val geofenceList = createGeofenceList(geofences, onFailure)

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL or GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofences(geofenceList)
                .build()

            geofencingClient.addGeofences(geofencingRequest, pendingIntent).apply {
                addOnSuccessListener {
                    Log.e(TAG, "geofence added successfully")
                    onSuccess()
                }
                addOnFailureListener { onFailure(it) }
            }

    }

    override fun removeAllGeofences(pendingIntent: PendingIntent, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        geofencingClient.removeGeofences(pendingIntent).apply {
            addOnSuccessListener { onSuccess() }
            addOnFailureListener { e ->
                onFailure(e)
            }
        }
    }

    override fun removeAndAddGeofences(
        geofences: List<GeofenceData>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val pendingIntent = createPendingIntent(context, GeofenceBroadcastReceiver::class.java, GeofenceConstants.GEO_LOCATION_INTENT_ACTION)
        removeAllGeofences(pendingIntent, {
            addGeofences(geofences, pendingIntent, onSuccess, onFailure)
        },{
            onFailure(it)
        })
    }

    /**
     * Converts a list of GeofenceData objects to a list of Geofence objects.
     *
     * @param geofences List of geofence data to be converted.
     * @param onFailure Callback invoked when there is a failure in creating a geofence.
     * @return List of Geofence objects.
     */
    private fun createGeofenceList(geofences: List<GeofenceData>, onFailure: (Exception) -> Unit): List<Geofence> {
        return geofences.mapNotNull { it.toGeofence(onFailure) }
    }

    private fun createPendingIntent(context: Context, receiverClass: Class<*>, action : String): PendingIntent {
        val intent = Intent(context, receiverClass)
        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}
