package com.apnamart.geofencing_module.geofencing.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apnamart.geofencing_module.geofencing.library.GeofenceModule

class ScheduleGeofenceWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        GeofenceModule.addGeofence()
        return Result.success()
    }
}

