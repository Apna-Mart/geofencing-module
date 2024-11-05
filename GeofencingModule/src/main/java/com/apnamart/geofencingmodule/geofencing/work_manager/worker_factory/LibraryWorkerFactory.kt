package com.apnamart.geofencingmodule.geofencing.work_manager.worker_factory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface LibraryWorkerFactory {
    fun create(appContext: Context, params : WorkerParameters) : ListenableWorker
}