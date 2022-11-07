/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.tickertape.alert.workmanager.worker

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.pyamsoft.tickertape.alert.work.inject.BaseInjector
import com.pyamsoft.tickertape.alert.work.params.BaseParameters
import com.pyamsoft.tickertape.alert.work.runner.WorkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal abstract class BaseWorker<P : BaseParameters>
protected constructor(context: Context, params: WorkerParameters) :
    CoroutineWorker(context.applicationContext, params) {

  final override suspend fun doWork(): Result =
      withContext(context = Dispatchers.Default) {
        val injector = getInjector(applicationContext)
        return@withContext when (val result =
            injector.execute(id, tags.toSet(), getParams(inputData))) {
          is WorkResult.Success -> {
            Timber.d("Work succeeded ${result.id}")
            Result.success()
          }
          is WorkResult.Cancel -> {
            Timber.w("Work cancelled: ${result.id}")

            // Return success so that the work chain continues, even though the work was cancelled
            Result.success()
          }
          is WorkResult.Failure -> {
            Timber.e("Work failed: ${result.id}")
            Result.failure()
          }
        }
      }

  @CheckResult protected abstract fun getInjector(context: Context): BaseInjector<P>

  @CheckResult protected abstract fun getParams(data: Data): P
}
