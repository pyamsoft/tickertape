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

package com.pyamsoft.tickertape.stocks.yahoo.source

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.yahoo.YahooApi
import com.pyamsoft.tickertape.stocks.yahoo.service.ChartService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class YahooChartSource
@Inject
internal constructor(@YahooApi private val service: ChartService) : ChartSource {

  override suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): List<StockChart> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val interval = getIntervalForRange(range)
        val result =
            service.getCharts(
                symbols = symbols.joinToString(",") { it.raw },
                range = range.apiValue,
                interval = interval.apiValue,
            )

        val localId = ZoneId.systemDefault()
        return@withContext result.spark.result
            .asSequence()
            .filterOnlyValidCharts()
            // Remove duplicate listings
            .distinctBy { it.symbol }
            .map { resp ->
              val chart = resp.response.first()
              val meta = chart.meta.requireNotNull()
              val currentDate = parseMarketTime(meta.regularMarketTime.requireNotNull(), localId)
              val currentPrice = meta.regularMarketPrice.requireNotNull().asMoney()
              val startingPrice = meta.chartPreviousClose.requireNotNull().asMoney()
              val timestamps = chart.timestamp.requireNotNull()
              val quote = chart.indicators.requireNotNull().quote.requireNotNull().first()

              val dates = timestamps.map { parseMarketTime(it, localId) }
              val closes = quote.close.requireNotNull()

              val validDates = mutableListOf<LocalDateTime>()
              val validClose = mutableListOf<StockMoneyValue>()

              // Some cryptocurrencies have nulls in the open, close, high, low
              // filter those points out
              for (i in dates.indices) {
                val date = dates[i]
                val close = closes[i] ?: continue

                validDates.add(date)
                validClose.add(close.asMoney())
              }

              return@map StockChart.create(
                  symbol = resp.symbol.asSymbol(),
                  range = range,
                  interval = interval,
                  currentPrice = currentPrice,
                  startingPrice = startingPrice,
                  dates = validDates,
                  close = validClose,
                  currentDate = currentDate,
              )
            }
            .toList()
      }

  companion object {

    @JvmStatic
    @CheckResult
    private fun getIntervalForRange(range: StockChart.IntervalRange): StockChart.IntervalTime {
      return when (range) {
        StockChart.IntervalRange.ONE_DAY -> StockChart.IntervalTime.ONE_MINUTE
        StockChart.IntervalRange.FIVE_DAY -> StockChart.IntervalTime.FIFTEEN_MINUTES
        StockChart.IntervalRange.ONE_MONTH -> StockChart.IntervalTime.SIXTY_MINUTES
        StockChart.IntervalRange.THREE_MONTH -> StockChart.IntervalTime.ONE_DAY
        StockChart.IntervalRange.SIX_MONTH -> StockChart.IntervalTime.ONE_DAY
        StockChart.IntervalRange.ONE_YEAR -> StockChart.IntervalTime.ONE_DAY
        StockChart.IntervalRange.TWO_YEAR -> StockChart.IntervalTime.FIVE_DAYS
        StockChart.IntervalRange.FIVE_YEAR -> StockChart.IntervalTime.ONE_WEEK
        StockChart.IntervalRange.TEN_YEAR -> StockChart.IntervalTime.ONE_MONTH
        StockChart.IntervalRange.YTD -> StockChart.IntervalTime.ONE_DAY
        StockChart.IntervalRange.MAX -> StockChart.IntervalTime.ONE_DAY
      }
    }
  }
}
