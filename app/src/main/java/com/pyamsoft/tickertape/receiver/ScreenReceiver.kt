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

package com.pyamsoft.tickertape.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.receiver.ScreenReceiver.Registration
import com.pyamsoft.tickertape.tape.TapeService
import timber.log.Timber

internal class ScreenReceiver internal constructor() : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action === Intent.ACTION_SCREEN_ON) {
      Timber.d("Start service on screen on")
      TapeService.start(context)
    }
  }

  fun interface Registration {

    fun unregister()
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun register(context: Context): Registration {
      val receiver = ScreenReceiver()
      val filter = IntentFilter().apply { addAction(Intent.ACTION_SCREEN_ON) }

      var registered = false
      val appContext = context.applicationContext

      Timber.d("Register new ScreenReceiver")
      appContext.registerReceiver(receiver, filter)

      return Registration {
        if (registered) {
          Timber.d("Unregister new ScreenReceiver")
          registered = false
          appContext.unregisterReceiver(receiver)
        }
      }
    }
  }
}
