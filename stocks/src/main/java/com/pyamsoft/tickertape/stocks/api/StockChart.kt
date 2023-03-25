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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.data.StockChartImpl
import java.time.LocalDateTime

@Stable
interface StockChart {

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val range: IntervalRange

  @get:CheckResult val interval: IntervalTime

  @get:CheckResult val startingPrice: StockMoneyValue

  @get:CheckResult val currentPrice: StockMoneyValue

  @get:CheckResult val currentDate: LocalDateTime

  @get:CheckResult val dates: List<LocalDateTime>

  @get:CheckResult val close: List<StockMoneyValue>

  @Stable
  @Immutable
  enum class IntervalTime(val apiValue: String, val display: String) {
    ONE_MINUTE("1m", "1 Minute"),
    TWO_MINUTES("2m", "2 Minutes"),
    FIFTEEN_MINUTES("15m", "15 Minutes"),
    SIXTY_MINUTES("60m", "1 Hour"),
    ONE_DAY("1d", "1 Day"),
    FIVE_DAYS("5d", "5 Days"),
    ONE_WEEK("1wk", "1 Week"),
    ONE_MONTH("1mo", "1 Month"),
  }

  @Stable
  @Immutable
  enum class IntervalRange(val apiValue: String, val display: String) {
    ONE_DAY("1d", "1 Day"),
    FIVE_DAY("5d", "5 Days"),
    ONE_MONTH("1mo", "1 Month"),
    THREE_MONTH("3mo", "3 Months"),
    SIX_MONTH("6mo", "6 Months"),
    ONE_YEAR("1y", "1 Year"),
    TWO_YEAR("2y", "2 Years"),
    FIVE_YEAR("5y", "5 Years"),
    TEN_YEAR("10y", "10 Years"),
    YTD("ytd", "Year to Date"),
    MAX("max", "Max")
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        range: IntervalRange,
        interval: IntervalTime,
        dates: List<LocalDateTime>,
        currentPrice: StockMoneyValue,
        startingPrice: StockMoneyValue,
        currentDate: LocalDateTime,
        close: List<StockMoneyValue>,
    ): StockChart {
      return StockChartImpl(
          symbol = symbol,
          range = range,
          interval = interval,
          dates = dates,
          currentPrice = currentPrice,
          startingPrice = startingPrice,
          currentDate = currentDate,
          close = close,
      )
    }
  }
}

@CheckResult
@WorkerThread
fun StockChart.periodHigh(): StockMoneyValue {
  Enforcer.assertOffMainThread()

  return close.maxByOrNull { it.value }.requireNotNull()
}

@CheckResult
@WorkerThread
fun StockChart.periodLow(): StockMoneyValue {
  Enforcer.assertOffMainThread()

  return close.minByOrNull { it.value }.requireNotNull()
}
