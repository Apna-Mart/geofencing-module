package com.apnamart.geofencingmodule.geofencing.permissions

/**
 * Callback interface to handle location permission results.
 */
interface PermissionCallback {
    fun onPermissionGranted() // Callback invoked when permissions are granted
    fun onPermissionDenied()   // Callback invoked when permissions are denied
}
