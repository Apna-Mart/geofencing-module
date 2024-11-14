package com.apnamart.geofencing_module.geofencing.work_manager.worker_utils

import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleOneTimeWorkerWithInitialDelay(
    workManager: WorkManager,
    tag: String,
    workerName: String,
    existingWorkPolicy: ExistingWorkPolicy,
    workerClass: Class<out CoroutineWorker>,
    constraints: Constraints? = null,
    delayValue : Long,
    delayUnit : TimeUnit
    ) {
    val worker = OneTimeWorkRequest.Builder(workerClass).apply {
        setInitialDelay(delayValue,delayUnit )
        addTag(tag)
        constraints?.let { setConstraints(it) }
    }.build()
    workManager.beginUniqueWork(workerName, existingWorkPolicy, worker).enqueue()

}
fun scheduleOneTimeWorkerWithOutData(
    workManager: WorkManager,
    tag: String,
    workerName: String,
    existingWorkPolicy: ExistingWorkPolicy,
    workerClass: Class<out CoroutineWorker>,
    constraints: Constraints? = null,
) {
    val worker = OneTimeWorkRequest.Builder(workerClass).apply {
        addTag(tag)
        constraints?.let { setConstraints(it) }
    }.build()
    workManager.beginUniqueWork(workerName, existingWorkPolicy, worker).enqueue()

}

fun schedulePeriodicWorkerWithConstraints(
    workManager: WorkManager,
    tag: String,
    workerName: String,
    existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
    duration: Pair<Long, TimeUnit>,
    workerClass: Class<out CoroutineWorker>,
    constraints: Constraints,
) {
    val worker =
        PeriodicWorkRequest.Builder(
            workerClass, duration.first,
            duration.second
        ).addTag(tag).setConstraints(constraints).build()

    workManager.enqueueUniquePeriodicWork(
        workerName, existingPeriodicWorkPolicy, worker
    )
}