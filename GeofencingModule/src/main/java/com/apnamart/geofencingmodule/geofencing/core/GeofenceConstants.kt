package com.apnamart.geofencingmodule.geofencing.core

object GeofenceConstants {
    // Geofence transition types
    const val GEOFENCE_TRANSITION_ENTER = 1
    const val GEOFENCE_TRANSITION_EXIT = 2
    const val GEOFENCE_TRANSITION_DWELL = 4

    // Geofence error messages
    const val ERROR_GEOFENCE_NOT_AVAILABLE = "Geofences are not available"
    const val ERROR_TOO_MANY_GEOFENCES = "Too many geofences"
    const val ERROR_TOO_MANY_PENDING_INTENTS = "Too many pending intents"
    const val ERROR_UNKNOWN_GEOFENCE = "Unknown geofence error"

    // Error codes for geofencing
    const val ERROR_CODE_NOT_AVAILABLE = 1000
    const val ERROR_CODE_TOO_MANY_GEOFENCES = 1001
    const val ERROR_CODE_TOO_MANY_PENDING_INTENTS = 1002

    const val GEO_LOCATION_INTENT_ACTION = "geo_location"

    const val GEOFENCE_SERVICE_WORKER_JOB = "geofence_service_worker_job"
    const val GEOFENCE_SERVICE_WORKER = "geofence_service_worker"
    const val TAG = "GeofenceLibrary"

    const val WORKER_CLASS_NAME = "worker_class_name"


}