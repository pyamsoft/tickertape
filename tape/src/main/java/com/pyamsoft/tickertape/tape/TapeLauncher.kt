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
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class TapeLauncher
@Inject
internal constructor(
    @TapeInternalApi private val tapeStopBus: EventBus<TapeRemote.StopCommand>,
    private val context: Context,
    private val serviceClass: Class<out Service>,
    private val preferences: TapePreferences,
) {

  @JvmOverloads
  suspend fun start(options: Options? = null) =
      withContext(context = Dispatchers.Default) {
        val appContext = context.applicationContext
        val service =
            Intent(appContext, serviceClass).apply {
              options?.also { opts ->
                opts.index?.also { putExtra(TapeRemote.KEY_CURRENT_INDEX, it) }
                opts.forceRefresh?.also { putExtra(TapeRemote.KEY_FORCE_REFRESH, it) }
              }
            }

        // If its market time or the client has passed alwaysStart flag
        val canStart = options?.alwaysStart == true || itsMarketTime()

        if (preferences.listenForTapeNotificationChanged().first() && canStart) {
          Timber.d("Starting tape notification")
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(service)
          } else {
            appContext.startService(service)
          }
        } else {
          Timber.w("Stop tape notification because it is not enabled.")
          appContext.stopService(service)
          tapeStopBus.send(TapeRemote.StopCommand)
        }
      }

  companion object {

    @CheckResult
    private fun ZonedDateTime.cleanTimeTo(hour: Int, minute: Int? = null): ZonedDateTime {
      return this.withHour(hour).withSecond(0).withNano(0).run {
        if (minute != null) {
          withMinute(minute)
        } else {
          withMinute(0)
        }
      }
    }

    @CheckResult
    private fun DayOfWeek.isWeekend(): Boolean {
      return this == DayOfWeek.SATURDAY || this == DayOfWeek.SUNDAY
    }

    @JvmStatic
    @CheckResult
    private fun itsMarketTime(): Boolean {
      // NYSE decides if the market is "open"
      val marketTime = ZonedDateTime.now(ZoneId.of("America/New_York"))

      // Weekend, no market
      if (marketTime.dayOfWeek.isWeekend()) {
        return false
      }

      val marketOpen = marketTime.cleanTimeTo(9, 30)
      if (marketTime.isBefore(marketOpen)) {
        return false
      }

      val marketClose = marketTime.cleanTimeTo(12 + 4)
      if (marketTime.isAfter(marketClose)) {
        return false
      }

      // TODO closed on holidays
      return true
    }
  }

  data class Options
  @JvmOverloads
  constructor(
      val index: Int? = null,
      val forceRefresh: Boolean? = null,
      val alwaysStart: Boolean? = null,
  )
}
