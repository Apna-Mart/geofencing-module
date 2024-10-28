package com.apnamart.geofencingmodule.geofencing

import android.Manifest
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.apnamart.geofencingmodule.geofencing.geofence_entity.GeofenceData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceHelper(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    fun createAndAddGeofence(
        geofenceList: List<GeofenceData>,
        receiverClass: Class<*>,
        action : String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val intent = createPendingIntent(context, receiverClass, action)

        val list = createGeofence(geofenceList, onFailure)

        removeAndAddGeofence(list, intent, onSuccess, onFailure)
    }

    private fun createGeofence(
        geofenceList: List<GeofenceData>,
        onFailure: (Exception) -> Unit
    ): List<Geofence> {
        return geofenceList.mapNotNull { it.toGeofence(
            onFailure
        ) }
    }

    private fun GeofenceData.toGeofence(
        onFailure: (Exception) -> Unit
    ): Geofence? {
        return try {
            val geofence = Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(
                    latitude, longitude,
                    radius
                )
                .setTransitionTypes(transactionType)
            delay?.let {
                geofence.setLoiteringDelay(it)
            }
            geofence.build()
        } catch (e: Exception) {
            onFailure.invoke(e)
            null
        }
    }

    private fun createPendingIntent(context: Context, receiverClass: Class<*>, action : String): PendingIntent {
        val intent = Intent(context, receiverClass)
        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    //todo: add a use case for checking locations permissions and handling it such as showing modal to user or something else
    private fun addGeofence(
        geofencingList: List<Geofence>,
        geofencingPendingIntent: PendingIntent,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofences(geofencingList.toList())
        }.build()

        val fineAndCoarseLocation = checkLocationPermission(context)
        val backgroundLocation =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && checkBackgroundLocationPermission(context))
        if (!fineAndCoarseLocation) {
            return
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocation) {
            return
        }

        geofencingClient.addGeofences(geofencingRequest, geofencingPendingIntent)
            .run {
                addOnSuccessListener {
                    onSuccess.invoke()
                }
                addOnFailureListener { ex ->
                   onFailure.invoke(ex)
                }
            }
    }

    private fun removeAndAddGeofence(
        geofencingList: List<Geofence>,
        geofencingPendingIntent: PendingIntent,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        removeGeofence(geofencingPendingIntent, onSuccess = {
            addGeofence(geofencingList, geofencingPendingIntent,
                onSuccess, onFailure)
        })
    }

    private fun removeGeofence(geofencingPendingIntent: PendingIntent, onSuccess : () -> Unit) {
        geofencingClient.removeGeofences(geofencingPendingIntent).run {
            addOnSuccessListener {
                onSuccess.invoke()
            }
            addOnFailureListener {
            }
        }
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission(context: Context): Boolean {
        return (checkPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && checkPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun checkBackgroundLocationPermission(context: Context): Boolean{
        return checkPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

}
