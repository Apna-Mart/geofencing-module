package com.apnamart.geofencingmodule.geofencing.library

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManagerImpl
import com.apnamart.geofencingmodule.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencingmodule.geofencing.provider.GeofenceDataProvider
import com.apnamart.geofencingmodule.geofencing.work_manager.AddGeofenceWorker
import com.apnamart.geofencingmodule.geofencing.work_manager.WorkManagerInitializer
import com.apnamart.geofencingmodule.geofencing.work_manager.worker_utils.scheduleOneTimeWorkerWithOutData
import com.apnamart.geofencingmodule.geofencing.work_manager.worker_utils.schedulePeriodicWorkerWithConstraints
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

object GeofenceLibrary {

    private var dataProvider: GeofenceDataProvider? = null
    private var eventHandler: GeofenceEventHandler? = null

    private var workManagerInitializer: WorkManagerInitializer? = null

    private var geofenceManager: GeofenceManager? = null

    private var workManager: WorkManager? = null

    fun initialize(
        context: Context,
        dataProvider: GeofenceDataProvider,
        eventHandler: GeofenceEventHandler
    ) {

        workManagerInitializer = WorkManagerInitializer(WeakReference(context))

        this.dataProvider = dataProvider
        this.eventHandler = eventHandler
        geofenceManager = GeofenceManagerImpl(context)

        workManager = workManagerInitializer?.workManager

        scheduleGeofenceWorker(context)
    }

    private fun scheduleGeofenceWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
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

    internal fun getEventHandler(): GeofenceEventHandler? {
        return eventHandler
    }

    internal fun getGeofenceDataProvider(): GeofenceDataProvider? {
        return dataProvider
    }

    internal fun getGeofenceManager(): GeofenceManager? {
        return geofenceManager
    }

    fun cancelGeofenceWorkers() {
        workManagerInitializer?.workManager?.cancelAllWorkByTag(GeofenceConstants.GEOFENCE_SERVICE_WORKER_JOB)
    }

    fun addGeofence() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        workManager?.let {
            scheduleOneTimeWorkerWithOutData(
                it,
                GeofenceConstants.GEOFENCE_SERVICE_WORKER_JOB,
                GeofenceConstants.GEOFENCE_SERVICE_ONE_TIME_WORKER,
                ExistingWorkPolicy.REPLACE,
                AddGeofenceWorker::class.java,
                constraints,
            )
        }
    }


}
