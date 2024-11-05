package com.apnamart.geofencingmodule.geofencing.library

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManagerImpl
import com.apnamart.geofencingmodule.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencingmodule.geofencing.provider.GeofenceDataProvider
import com.apnamart.geofencingmodule.geofencing.work_manager.AddGeofenceWorker
import com.apnamart.geofencingmodule.geofencing.work_manager.WorkManagerInitializer
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

object GeofenceLibrary {

    private var dataProvider: GeofenceDataProvider? = null
    private  var eventHandler: GeofenceEventHandler? = null

    private var workManagerInitializer: WorkManagerInitializer? = null

    private  var geofenceManager: GeofenceManager? = null

    fun initialize(
        context: Context,
        dataProvider: GeofenceDataProvider,
        eventHandler: GeofenceEventHandler
    ) {

        workManagerInitializer = WorkManagerInitializer(WeakReference(context))

        this.dataProvider = dataProvider
        this.eventHandler = eventHandler
        geofenceManager = GeofenceManagerImpl(context)

        scheduleGeofenceWorker(context)
    }

    private fun scheduleGeofenceWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workManager = workManagerInitializer?.workManager

        workManager?.let {
            schedulePeriodicWorkerWithConstraints(
                it,
                GeofenceConstants.GEOFENCE_SERVICE_WORKER_JOB,
                GeofenceConstants.GEOFENCE_SERVICE_WORKER,
                ExistingPeriodicWorkPolicy.KEEP,
                Pair(15, TimeUnit.MINUTES),
                Pair(5, TimeUnit.MINUTES),
                AddGeofenceWorker::class.java,
                constraints
            )
        }
    }

    fun getEventHandler(): GeofenceEventHandler? {
        return eventHandler
    }

    fun getGeofenceDataProvider(): GeofenceDataProvider? {
        return dataProvider
    }

    fun getGeofenceManager(): GeofenceManager? {
        return geofenceManager
    }

    private fun schedulePeriodicWorkerWithConstraints(
        workManager: WorkManager,
        tag: String,
        workerName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
        duration: Pair<Long, TimeUnit>,
        flexDuration: Pair<Long, TimeUnit>,
        workerClass: Class<out CoroutineWorker>,
        constraints: Constraints,
    ) {
        val worker =
            PeriodicWorkRequest.Builder(
                workerClass, duration.first,
                duration.second, flexDuration.first,
                flexDuration.second,
            ).addTag(tag).setConstraints(constraints).build()

        workManager.enqueueUniquePeriodicWork(
            workerName, existingPeriodicWorkPolicy, worker
        )
    }
}
