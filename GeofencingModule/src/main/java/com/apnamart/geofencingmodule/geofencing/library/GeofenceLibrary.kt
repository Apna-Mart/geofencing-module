package com.apnamart.geofencingmodule.geofencing.library

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.WORKER_CLASS_NAME
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManagerImpl
import com.apnamart.geofencingmodule.geofencing.di.AppModule
import com.apnamart.geofencingmodule.geofencing.di.DaggerLibraryComponent
import com.apnamart.geofencingmodule.geofencing.di.LibraryComponent
import com.apnamart.geofencingmodule.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper
import com.apnamart.geofencingmodule.geofencing.provider.GeofenceDataProvider
import com.apnamart.geofencingmodule.geofencing.work_manager.GeofenceWorker
import com.apnamart.geofencingmodule.geofencing.work_manager.WorkManagerInitializer
import com.apnamart.geofencingmodule.geofencing.work_manager.proxy_worker.ProxyWorker
import dagger.multibindings.IntKey
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

object GeofenceLibrary {

    private lateinit var dataProvider: GeofenceDataProvider
    private lateinit var eventHandler: GeofenceEventHandler

    private var workManagerInitializer: WorkManagerInitializer? = null

    fun initialize(
        context: Context,
        dataProvider: GeofenceDataProvider,
        eventHandler: GeofenceEventHandler
    ) {

        workManagerInitializer = WorkManagerInitializer(WeakReference(context))

        Log.e(GeofenceConstants.TAG, "library initialised")
//
//        libraryComponent = DaggerLibraryComponent.builder()
//            .appModule(AppModule(context, dataProvider, eventHandler))
//            .build()
//
//        libraryComponent.inject(this)

        this.dataProvider = dataProvider
        this.eventHandler = eventHandler

//        registerWorkerFactories(context)

        // Schedule the GeofenceWorker to run every 15 minutes
        scheduleGeofenceWorker(context)
    }


    private fun registerWorkerFactories(context: Context) {
        // Registering the GeofenceWorkerFactory
        Log.e(GeofenceConstants.TAG, "library worker factory register")
//        ProxyWorker.registerWorkerFactory(
//            GeofenceWorker::class.qualifiedName!!,
//            GeofenceWorkerFactory(
//                GeofenceManagerImpl(context),
//                dataProvider,
//                LocationPermissionHelper
//            )
//        )
        // Add other factories as needed
    }

    private fun scheduleGeofenceWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        Log.d(GeofenceConstants.TAG, "library work request")

//        val workerData = Data.Builder()
//            .putString(WORKER_CLASS_NAME, GeofenceWorker::class.qualifiedName).build()
        val workManager = workManagerInitializer?.workManager
//
//        // Create the worker request with the factory method
//        val workRequest = PeriodicWorkRequestBuilder<GeofenceWorker>(15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
//            .setConstraints(constraints)
//            .addTag(GeofenceConstants.GEOFENCE_SERVICE_WORKER_JOB)
//            .build()
//
//        workManager?.enqueueUniquePeriodicWork(
//            GeofenceConstants.GEOFENCE_SERVICE_WORKER,
//            ExistingPeriodicWorkPolicy.KEEP,
//            workRequest
//        )?: run {
//            Log.e(TAG, "WorkManager instance is null")
//        }

        workManager?.let {
            Log.e(TAG, "work manager not null")
            schedulePeriodicWorkerWithConstraints(
                it,
                GeofenceConstants.GEOFENCE_SERVICE_WORKER_JOB,
                GeofenceConstants.GEOFENCE_SERVICE_WORKER,
                ExistingPeriodicWorkPolicy.KEEP,
                Pair(15, TimeUnit.MINUTES),
                Pair(5, TimeUnit.MINUTES),
                GeofenceWorker::class.java,
                constraints
            )
        }
    }

    fun getEventHandler(): GeofenceEventHandler {
        return eventHandler
    }

    fun getGeofenceDataProvider(): GeofenceDataProvider {
        return dataProvider
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
