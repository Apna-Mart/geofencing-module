package com.apnamart.geofencingmodule.geofencing.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

/**
 * A utility class for managing location permissions.
 */
object LocationPermissionHelper {

    // Constants for permission request codes
    private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

    /**
     * Checks if the required location permissions are granted.
     *
     * @param context The context to check permissions against.
     * @return A list of missing permissions, if any.
     */
    fun checkLocationPermissions(context: Context): Boolean {
        val fineAndCoarseLocation = checkLocationPermission(context)
        val backgroundLocation =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && checkBackgroundLocationPermission(context))
        if (!fineAndCoarseLocation) {
            return false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocation) {
            return false
        }
        return true
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

    fun createLocation(provider: String, lat: Double, long: Double): Location {
        val location = Location(provider)
        location.latitude = lat
        location.longitude = long
        return location
    }
}
