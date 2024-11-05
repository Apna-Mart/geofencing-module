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

    /**
     * Requests the necessary location permissions.
     *
     * @param activity The activity context from which permissions are requested.
     * @param missingPermissions List of missing permissions to request.
     * @param callback Callback to handle permission results.
     * @param customDenialHandler Optional custom handler for when permissions are denied.
     */
    fun requestLocationPermissions(
        activity: Activity,
        missingPermissions: List<String>,
        callback: PermissionCallback,
        customDenialHandler: (() -> Unit)? = null
    ) {
        // If there are missing permissions, request them
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Invoke the callback if all permissions are granted
            callback.onPermissionGranted()
        }
    }

    /**
     * Handles the result of the permission request.
     *
     * @param requestCode The request code returned in onRequestPermissionsResult.
     * @param grantResults The results of the requested permissions.
     * @param callback Callback to handle permission results.
     */
    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        callback: PermissionCallback,
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the permissions were granted
            val allPermissionsGranted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allPermissionsGranted) {
                callback.onPermissionGranted() // Invoke callback on permission granted
            } else {
                callback.onPermissionDenied() // Invoke the default permission denied callback
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

    fun createLocation(provider: String, lat: Double, long: Double): Location {
        val location = Location(provider)
        location.latitude = lat
        location.longitude = long
        return location
    }
}
