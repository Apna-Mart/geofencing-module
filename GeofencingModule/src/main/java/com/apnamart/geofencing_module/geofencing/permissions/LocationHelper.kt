package com.apnamart.geofencing_module.geofencing.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A utility class for managing location permissions.
 */
object LocationHelper {

    /**
     * Checks if the required location permissions are granted.
     *
     * @param context The context to check permissions against.
     * @return A list of missing permissions, if any.
     */

    private fun getLocationClient(context: Context): SettingsClient {
        return LocationServices.getSettingsClient(context)
    }

    fun checkLocationPermissions(context: Context): Boolean {
        val fineAndCoarseLocation = checkFineAndCoarseLocationPermission(context)
        if (!fineAndCoarseLocation) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !checkBackgroundLocationPermission(
                context
            )
        ) {
            return false
        }
        return true
    }

    private fun checkPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkFineAndCoarseLocationPermission(context: Context): Boolean {
        return (checkPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && checkPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun checkBackgroundLocationPermission(context: Context): Boolean {
        return checkPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    fun createLocation(provider: String, lat: Double, long: Double): Location {
        val location = Location(provider)
        location.latitude = lat
        location.longitude = long
        return location
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(
        context: Context,
        scope: CoroutineScope,
        onFailure :(Exception) -> Unit
    ): Flow<Location?> = callbackFlow() {

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY , 10000)
            .setMaxUpdates(1)
            .build()

        val listener = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (!scope.isActive) {
                    close()
                }
                locationResult.locations.lastOrNull()?.let { location ->
                    trySend(location)
                }
            }
        }

        getLocationClient(context).checkLocationSettings(
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        )
            .addOnSuccessListener {
                try {
                    LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(
                        locationRequest,
                        listener,
                        Looper.getMainLooper()
                    ).addOnFailureListener {
                        close(it)
                        onFailure(it)
                        scope.launch {
                            trySend(null)
                        }
                    }.addOnCompleteListener { task ->
                        if (!task.isSuccessful || task.exception != null) {
                            task.exception?.let { onFailure(it)}
                            trySend(null)
                        }
                    }
                } catch (e: Exception) {
                    onFailure(e)
                    scope.launch {
                        trySend(null)
                    }
                }
            }.addOnFailureListener { e->
                onFailure(e)
                scope.launch {
                    trySend(null)
                }
            }
        awaitClose {
            LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(listener)
        }
    }

    suspend fun getLocation(
        context: Context,
        coroutineScope: CoroutineScope,
        onError: (Exception) -> Unit
    ): Location? {
        if (!checkLocationPermissions(context)){
            onError(Exception("Location permissions not granted"))
            return null
        }
        return try {
            getLastLocation(context, coroutineScope, onError).firstOrNull()
        } catch (exception: Exception) {
            onError(exception)
            null
        }
    }
}
