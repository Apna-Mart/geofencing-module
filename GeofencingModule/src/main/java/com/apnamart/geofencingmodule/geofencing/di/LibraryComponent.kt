package com.apnamart.geofencingmodule.geofencing.di

import com.apnamart.geofencingmodule.geofencing.library.GeofenceLibrary
import com.apnamart.geofencingmodule.geofencing.work_manager.LibraryWorkerFactoryProvider
import com.apnamart.geofencingmodule.geofencing.work_manager.worker_factory.GeofenceLibraryWorkerModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@LibraryScope
@Component(modules = [AppModule::class])
interface LibraryComponent {

    fun inject(library : GeofenceLibrary)
}
