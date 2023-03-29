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

package com.pyamsoft.tickertape.alert.base

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.alert.WorkResult
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal abstract class BaseRunner<P : BaseWorkerParameters> protected constructor() {

  // Don't mark inline or you get an Inaccessible error from the JVM at runtime
  @CheckResult
  suspend fun doWork(
      id: UUID,
      tags: Set<String>,
      params: P,
  ): WorkResult =
      withContext(context = Dispatchers.Default) {
        val identifier = identifier(id, tags)
        try {
          performWork(params)
          success(identifier)
        } catch (e: Throwable) {
          if (e is CancellationException) {
            cancelled(identifier, e)
          } else {
            fail(identifier, e)
          }
        } finally {
          Timber.d("Worker has been completed")
        }
      }

  protected abstract suspend fun performWork(params: P)

  @CheckResult
  private fun success(identifier: String): WorkResult {
    Timber.d("Worker completed successfully $identifier")
    return WorkResult.Success(identifier)
  }

  @CheckResult
  private fun fail(identifier: String, throwable: Throwable): WorkResult {
    Timber.e(throwable, "Worker failed to complete $identifier")
    return WorkResult.Failure(identifier)
  }

  @CheckResult
  private fun cancelled(identifier: String, throwable: CancellationException): WorkResult {
    Timber.w(throwable, "Worker was cancelled $identifier")
    return WorkResult.Cancel(identifier)
  }

  @CheckResult
  private fun identifier(id: UUID, tags: Set<String>): String {
    return "[ id=$id, tags=$tags ]"
  }
}
