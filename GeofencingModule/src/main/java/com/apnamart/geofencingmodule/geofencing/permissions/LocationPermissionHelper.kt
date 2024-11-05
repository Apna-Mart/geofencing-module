package com.apnamart.geofencingmodule.geofencing.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * A utility class for managing location permissions.
 */
object LocationPermissionHelper {

    /**
     * Checks if the required location permissions are granted.
     *
     * @param context The context to check permissions against.
     * @return A list of missing permissions, if any.
     */
    fun checkLocationPermissions(context: Context): Boolean {
        val fineAndCoarseLocation = checkFineAndCoarseLocationPermission(context)
        if (!fineAndCoarseLocation) {
            return false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !checkBackgroundLocationPermission(context)) {
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
    fun checkBackgroundLocationPermission(context: Context): Boolean{
        return checkPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    fun createLocation(provider: String, lat: Double, long: Double): Location {
        val location = Location(provider)
        location.latitude = lat
        location.longitude = long
        return location
    }
}
