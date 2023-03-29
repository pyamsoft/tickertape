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

package com.pyamsoft.tickertape.worker.workmanager.workers

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pyamsoft.tickertape.worker.work.BgWorker
import com.pyamsoft.tickertape.worker.workmanager.WorkerComponent
import com.pyamsoft.tickertape.worker.workmanager.WorkerObjectGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class AbstractJobWorker
protected constructor(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

  private fun inject() {
    onInject(WorkerObjectGraph.retrieve(applicationContext))
  }

  private fun destroy() {
    onDestroy()
  }

  @CheckResult
  private suspend fun process(worker: BgWorker): Result {
    val tags = this.tags

    return when (val result = worker.work()) {
      is BgWorker.WorkResult.Cancelled -> {
        Timber.w("Work was cancelled, report success to avoid retry policy: $tags")
        Result.success()
      }
      is BgWorker.WorkResult.Failed -> {
        Timber.e(result.throwable, "Work failed to complete: $tags")
        Result.failure()
      }
      is BgWorker.WorkResult.Success -> {
        Timber.d("Work succeeded: $tags")
        Result.success()
      }
    }
  }

  final override suspend fun doWork(): Result =
      withContext(context = Dispatchers.Default) {
        try {
          inject()

          return@withContext process(worker())
        } catch (e: Throwable) {
          Timber.e(e, "Error running work")
          return@withContext Result.failure()
        } finally {
          destroy()
        }
      }

  protected abstract fun onInject(component: WorkerComponent)

  protected abstract fun onDestroy()

  protected abstract fun worker(): BgWorker
}
