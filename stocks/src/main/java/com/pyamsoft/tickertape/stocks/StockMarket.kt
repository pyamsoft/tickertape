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

package com.pyamsoft.tickertape.stocks

import androidx.annotation.CheckResult
import java.time.Clock
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

/** Decide if the US stock market is open */
object StockMarket {

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
  fun isOpen(clock: Clock): Boolean {
    // NYSE decides if the market is "open"
    val marketTime = ZonedDateTime.now(clock.withZone(ZoneId.of("America/New_York")))

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
