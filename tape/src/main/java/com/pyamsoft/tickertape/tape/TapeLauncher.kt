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

@Singleton
class TapeLauncher
@Inject
internal constructor(private val context: Context, private val serviceClass: Class<out Service>) {

  @JvmOverloads
  fun start(options: TapeRemote.NotificationOptions? = null) {
    val appContext = context.applicationContext
    val service =
        Intent(appContext, serviceClass).apply {
          options?.also { opts ->
            putExtra(TapeRemote.KEY_CURRENT_INDEX, opts.index)
            putExtra(TapeRemote.KEY_FORCE_REFRESH, opts.forceRefresh)
          }
        }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      appContext.startForegroundService(service)
    } else {
      appContext.startService(service)
    }
  }
}