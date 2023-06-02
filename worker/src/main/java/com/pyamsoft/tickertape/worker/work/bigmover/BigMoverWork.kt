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

package com.pyamsoft.tickertape.worker.work.bigmover

import com.pyamsoft.tickertape.worker.work.BgWorker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class BigMoverWork
@Inject
internal constructor(
    private val standalone: BigMoverStandalone,
) : BgWorker {

  override suspend fun work(): BgWorker.WorkResult =
      withContext(context = Dispatchers.Default) {
        try {
          standalone.notifyBigMovers()
          return@withContext BgWorker.WorkResult.Success
        } catch (e: Throwable) {
          if (e is CancellationException) {
            Timber.w("Job cancelled during processing")
            return@withContext BgWorker.WorkResult.Cancelled
          } else {
            Timber.e(e, "Error during processing of repeats to create transactions")
            return@withContext BgWorker.WorkResult.Failed(e)
          }
        }
      }
}
