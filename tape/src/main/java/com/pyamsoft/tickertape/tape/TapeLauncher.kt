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

package com.pyamsoft.tickertape.tape

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class TapeLauncher
@Inject
internal constructor(
    private val context: Context,
    private val serviceClass: Class<out Service>,
    private val preferences: TapePreferences,
) {

  @JvmOverloads
  suspend fun start(options: Options? = null) =
      withContext(context = Dispatchers.Default) {
        if (!preferences.isTapeNotificationEnabled()) {
          Timber.w("Not launching Tape service since it is not enabled")
          return@withContext
        }

        val appContext = context.applicationContext
        val service =
            Intent(appContext, serviceClass).apply {
              options?.also { opts ->
                opts.index?.also { i -> putExtra(TapeRemote.KEY_CURRENT_INDEX, i) }
                opts.forceRefresh?.also { f -> putExtra(TapeRemote.KEY_FORCE_REFRESH, f) }
              }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          appContext.startForegroundService(service)
        } else {
          appContext.startService(service)
        }
      }

  data class Options(val index: Int?, val forceRefresh: Boolean?)
}
