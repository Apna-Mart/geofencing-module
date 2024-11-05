package com.apnamart.geofencingmodule.geofencing.work_manager

import androidx.work.ListenableWorker
import com.apnamart.geofencingmodule.geofencing.work_manager.worker_factory.GeofenceLibraryWorkerModule
import com.apnamart.geofencingmodule.geofencing.work_manager.worker_factory.LibraryWorkerFactory
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module
object LibraryWorkerFactoryProvider {

//    @Provides
//    @LibraryWorkerFactories
//    fun provideLibraryWorkerFactories(
//        workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<LibraryWorkerFactory>>
//    ): Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<LibraryWorkerFactory>> {
//        return workerFactories
//    }
}