/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.worker.workmanager

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.await
import com.pyamsoft.tickertape.worker.WorkJobType
import com.pyamsoft.tickertape.worker.WorkerQueue
import com.pyamsoft.tickertape.worker.workmanager.workers.BigMoverWorker
import com.pyamsoft.tickertape.worker.workmanager.workers.PriceAlertWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class WorkerQueueImpl
@Inject
internal constructor(
    private val context: Context,
) : WorkerQueue {

  override suspend fun enqueue(type: WorkJobType) =
      withContext(context = Dispatchers.IO) {
        val builder: WorkRequest.Builder<*, *> =
            when (type) {
              WorkJobType.REPEAT_BIG_MOVERS ->
                  PeriodicWorkRequestBuilder<BigMoverWorker>(
                      // Repeat once every 30 minutes
                      30L,
                      TimeUnit.MINUTES,
                  )
              WorkJobType.REPEAT_PRICE_ALERTS ->
                  PeriodicWorkRequestBuilder<PriceAlertWorker>(
                      // Repeat once every 15 minutes
                      15L,
                      TimeUnit.MINUTES,
                  )
            }

        val work = builder.addTag(type.name).build()
        Timber.d("Enqueue work: $type")

        // Resolve the WorkManager instance
        try {
          WorkManager.getInstance(context).enqueue(work).await()
        } catch (e: Throwable) {
          Timber.e(e, "Error queueing work: $type")
        }

        // No return
        return@withContext
      }

  override suspend fun cancel(type: WorkJobType) =
      withContext(context = Dispatchers.IO) {

        // Resolve the WorkManager instance
        Timber.d("Cancel work by tag: $type")
        try {
          WorkManager.getInstance(context).cancelAllWorkByTag(type.name).await()
        } catch (e: Throwable) {
          Timber.e(e, "Error cancelling work: $type")
        }

        // No return
        return@withContext
      }
}
