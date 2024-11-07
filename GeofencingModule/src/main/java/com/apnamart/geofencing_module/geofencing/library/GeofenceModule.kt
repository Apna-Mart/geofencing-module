package com.apnamart.geofencing_module.geofencing.library

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.apnamart.geofencing_module.geofencing.broadcast_receiver.GeofenceBroadcastReceiver
import com.apnamart.geofencing_module.geofencing.core.GeofenceConstants
import com.apnamart.geofencing_module.geofencing.core.GeofenceManager
import com.apnamart.geofencing_module.geofencing.core.GeofenceManagerImpl
import com.apnamart.geofencing_module.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencing_module.geofencing.provider.GeofenceDataProvider
import com.apnamart.geofencing_module.geofencing.work_manager.AddGeofenceWorker
import com.apnamart.geofencing_module.geofencing.work_manager.WorkManagerInitializer
import com.apnamart.geofencing_module.geofencing.work_manager.worker_utils.scheduleOneTimeWorkerWithOutData
import com.apnamart.geofencing_module.geofencing.work_manager.worker_utils.schedulePeriodicWorkerWithConstraints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

object GeofenceModule {

    private var dataProvider: GeofenceDataProvider? = null
    private var eventHandler: GeofenceEventHandler? = null

    private var workManagerInitializer: WorkManagerInitializer? = null

    private var geofenceManager: GeofenceManager? = null

    private var workManager: WorkManager? = null

    val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

        scheduleGeofenceWorker()
    }

    private fun scheduleGeofenceWorker() {
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

    suspend fun cancelGeofenceWorkers(context: Context, onSuccess : () -> Unit, onFailure : (Exception) -> Unit) {

        val pendingIntent = createPendingIntent(context, GeofenceBroadcastReceiver::class.java, GeofenceConstants.GEO_LOCATION_INTENT_ACTION)

        geofenceManager?.removeAllGeofences(pendingIntent, onSuccess = {
                onSuccess()
            }, onFailure =  { e ->
                onFailure(e)
            })

        workManager?.cancelAllWorkByTag(GeofenceConstants.GEOFENCE_SERVICE_WORKER_JOB)
    }

    suspend fun addGeofence() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        workManager?.let {
            scheduleOneTimeWorkerWithOutData(
                it,
                GeofenceConstants.GEOFENCE_SERVICE_ONE_TIME_WORKER_JOB,
                GeofenceConstants.GEOFENCE_SERVICE_ONE_TIME_WORKER,
                ExistingWorkPolicy.REPLACE,
                AddGeofenceWorker::class.java,
                constraints,
            )
        }
    }

    fun createPendingIntent(context: Context, receiverClass: Class<*>, action : String): PendingIntent {
        val intent = Intent(context, receiverClass)
        intent.action = action
        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}
