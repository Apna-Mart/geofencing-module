package com.apnamart.geofencingmodule.geofencing.work_manager.worker_factory

import com.apnamart.geofencingmodule.geofencing.core.GeofenceManager
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper
import com.apnamart.geofencingmodule.geofencing.provider.GeofenceDataProvider
import com.apnamart.geofencingmodule.geofencing.work_manager.GeofenceWorker
import com.apnamart.geofencingmodule.geofencing.work_manager.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module
class GeofenceLibraryWorkerModule {

//    @Provides
//    @IntoMap
//    @WorkerKey(GeofenceWorker::class)
//    fun bindGeofenceWorkerFactory(
//        geofenceManager: GeofenceManager,
//        geofenceDataProvider: GeofenceDataProvider,
//        locationPermissionHelper: LocationPermissionHelper
//    ): LibraryWorkerFactory {
//        return GeofenceWorkerFactory(geofenceManager, geofenceDataProvider, locationPermissionHelper)
//    }
}