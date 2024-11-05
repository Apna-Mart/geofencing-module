package com.apnamart.geofencingmodule.geofencing.di

import android.content.Context
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManager
import com.apnamart.geofencingmodule.geofencing.core.GeofenceManagerImpl
import com.apnamart.geofencingmodule.geofencing.event_handler.GeofenceEventHandler
import com.apnamart.geofencingmodule.geofencing.permissions.LocationPermissionHelper
import com.apnamart.geofencingmodule.geofencing.provider.GeofenceDataProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(
    private val context : Context,
    private val dataProvider: GeofenceDataProvider,
    private val eventHandler: GeofenceEventHandler
) {

//    @Provides
//    @LibraryScope
//    fun provideContext(): Context {
//        return context
//    }
//
//    @Provides
//    @LibraryScope
//    fun provideGeofenceDataProvider(): GeofenceDataProvider {
//        return dataProvider
//    }
//
//    @Provides
//    @LibraryScope
//    fun provideGeofenceEventHandler(): GeofenceEventHandler {
//        return eventHandler
//    }
//
//    @Provides
//    @LibraryScope
//    fun provideGeofenceManager() : GeofenceManager = GeofenceManagerImpl(context)
//
//    @Provides
//    @LibraryScope
//    fun provideLocationPermissionHelper() : LocationPermissionHelper = LocationPermissionHelper

}