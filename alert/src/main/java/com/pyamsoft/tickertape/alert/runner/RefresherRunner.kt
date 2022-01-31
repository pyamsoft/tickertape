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

package com.pyamsoft.tickertape.alert.runner

import com.pyamsoft.tickertape.alert.params.RefreshParameters
import com.pyamsoft.tickertape.tape.TapeLauncher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

@Singleton
internal class RefresherRunner
@Inject
internal constructor(
    private val tapeLauncher: TapeLauncher,
) : BaseRunner<RefreshParameters>() {

  override suspend fun performWork(params: RefreshParameters) = coroutineScope {
    val force = params.forceRefresh

    try {
      tapeLauncher.start(
          options =
              TapeLauncher.Options(
                  index = null,
                  forceRefresh = force,
              ),
      )
    } catch (e: Throwable) {
      Timber.e(e, "Error refreshing quotes")
    }

    return@coroutineScope
  }
}
