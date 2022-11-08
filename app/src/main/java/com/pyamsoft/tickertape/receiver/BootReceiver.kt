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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.alert.AlarmFactory
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.initOnAppStart
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

internal class BootReceiver internal constructor() : BroadcastReceiver() {

  @Inject @JvmField internal var alerter: Alerter? = null
  @Inject @JvmField internal var alarmFactory: AlarmFactory? = null

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      Injector.obtainFromApplication<TickerComponent>(context).inject(this)

      MainScope().launch(context = Dispatchers.Default) {
        Timber.d("Schedule alarms on boot")
        alerter.requireNotNull().initOnAppStart(alarmFactory.requireNotNull())
      }
    }
  }

  companion object {

    @JvmStatic
    fun setEnabled(context: Context, enable: Boolean) {
      val appContext = context.applicationContext
      val name = ComponentName(appContext, BootReceiver::class.java)
      val state =
          if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
          } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
          }

      appContext.packageManager.setComponentEnabledSetting(
          name, state, PackageManager.DONT_KILL_APP)
    }

    @JvmStatic
    @CheckResult
    fun isEnabled(context: Context): Boolean {
      val appContext = context.applicationContext
      val name = ComponentName(appContext, BootReceiver::class.java)
      val state = appContext.packageManager.getComponentEnabledSetting(name)
      return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
  }
}
