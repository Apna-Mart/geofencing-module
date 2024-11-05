package com.apnamart.geofencingmodule.geofencing.work_manager.proxy_worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.TAG
import com.apnamart.geofencingmodule.geofencing.core.GeofenceConstants.WORKER_CLASS_NAME
import com.apnamart.geofencingmodule.geofencing.work_manager.worker_factory.LibraryWorkerFactory
import com.google.common.util.concurrent.ListenableFuture

class ProxyWorker constructor(
    appContext: Context,
    params: WorkerParameters,
) : ListenableWorker(appContext, params){

    private val workerClassName = params.inputData.getString(WORKER_CLASS_NAME) ?: ""

    // Here you can use the factory
    private val delegateWorkerFactory = getDelegateWorkerFactory()

    private val delegateWorker = delegateWorkerFactory?.createWorker(appContext, params)


    override fun startWork(): ListenableFuture<Result> {
        Log.d(TAG, "proxy worker start work")
        return if (delegateWorker != null) {
            Log.d(TAG, "proxy worker do work ${delegateWorker.javaClass}")
            delegateWorker.startWork()
        } else {
            throw IllegalArgumentException("No delegateWorker available")
        }
    }

    override fun onStopped() {
        super.onStopped()
        delegateWorker?.onStopped()
    }

    private fun getDelegateWorkerFactory(): LibraryWorkerFactory? {
        // Map of worker class names to their corresponding factories
        return workerFactories[workerClassName] // Retrieve the factory for the given worker class name
    }

    private fun LibraryWorkerFactory?.createWorker(
        appContext: Context,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return this?.create(appContext, workerParameters)
    }

    companion object {

        private val workerFactories = object : AbstractMutableMap<String, LibraryWorkerFactory>() {
            private val backingWorkerMap = mutableMapOf<String, LibraryWorkerFactory>()

            override fun put(key: String, value: LibraryWorkerFactory): LibraryWorkerFactory? {
                Log.d(TAG, "put woker factory $key")
                return backingWorkerMap.put(key, value)
            }

            override val entries: MutableSet<MutableMap.MutableEntry<String, LibraryWorkerFactory>>
                get() = backingWorkerMap.entries
        }

        // Function to register worker factories
        fun registerWorkerFactory(workerClassName: String, factory: LibraryWorkerFactory) {

            Log.d(TAG, "register woker factory $workerClassName")
            workerFactories[workerClassName] = factory
        }
    }

}
