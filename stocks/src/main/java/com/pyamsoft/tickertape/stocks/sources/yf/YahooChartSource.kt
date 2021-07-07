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

package com.pyamsoft.tickertape.stocks.sources.yf

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.InternalApi
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asVolume
import com.pyamsoft.tickertape.stocks.data.StockChartImpl
import com.pyamsoft.tickertape.stocks.network.NetworkChart
import com.pyamsoft.tickertape.stocks.service.ChartService
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooChartSource
@Inject
internal constructor(@InternalApi private val service: ChartService) : ChartSource {

  override suspend fun getChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
      includePrePost: Boolean
  ): StockChart =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val interval = getIntervalForRange(range)
        val result =
            service.getQuotes(
                url = getChartUrl(symbol),
                includePrePost = includePrePost,
                range = range.apiValue,
                interval = interval.apiValue)

        val zoneId = ZoneId.systemDefault()
        return@withContext result
            .chart
            .result
            .asSequence()
            .filterOnlyValidStockData()
            .map { chart ->
              val startingPrice =
                  chart.meta.requireNotNull().chartPreviousClose.requireNotNull().asMoney()
              val timestamps = chart.timestamp.requireNotNull()
              val quote = chart.indicators.requireNotNull().quote.requireNotNull().first()

              val dates =
                  timestamps.map { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), zoneId) }
              val volumes = quote.volume.requireNotNull()
              val opens = quote.open.requireNotNull()
              val closes = quote.close.requireNotNull()
              val highs = quote.high.requireNotNull()
              val lows = quote.low.requireNotNull()

              val validDates = mutableListOf<LocalDateTime>()
              val validVolume = mutableListOf<StockVolumeValue>()
              val validOpen = mutableListOf<StockMoneyValue>()
              val validClose = mutableListOf<StockMoneyValue>()
              val validHigh = mutableListOf<StockMoneyValue>()
              val validLow = mutableListOf<StockMoneyValue>()

              // Some cryptocurrencies have nulls in the open, close, high, low
              // filter those points out
              for (i in dates.indices) {
                val date = dates[i]
                val volume = volumes[i] ?: continue
                val open = opens[i] ?: continue
                val close = closes[i] ?: continue
                val high = highs[i] ?: continue
                val low = lows[i] ?: continue

                validDates.add(date)
                validVolume.add(volume.asVolume())
                validOpen.add(open.asMoney())
                validClose.add(close.asMoney())
                validHigh.add(high.asMoney())
                validLow.add(low.asMoney())
              }

              StockChartImpl(
                  symbol = symbol,
                  range = range,
                  interval = interval,
                  startingPrice = startingPrice,
                  dates = validDates,
                  volume = validVolume,
                  open = validOpen,
                  close = validClose,
                  low = validLow,
                  high = validHigh,
              )
            }
            .first()
      }

  companion object {

    private const val YF_QUOTE_SOURCE = "https://query1.finance.yahoo.com/v8/finance/chart"

    @JvmStatic
    @CheckResult
    private fun getChartUrl(symbol: StockSymbol): String {
      return "$YF_QUOTE_SOURCE/${symbol.symbol()}"
    }

    @JvmStatic
    @CheckResult
    private fun Sequence<NetworkChart>.filterOnlyValidStockData(): Sequence<NetworkChart> {
      return this.filter { it.isValidStockData() }
    }

    @JvmStatic
    @CheckResult
    private fun NetworkChart.isValidStockData(): Boolean {
      // We need all of these values to have a valid ticker
      if (meta?.chartPreviousClose == null) {
        return false
      }

      // We need indicators and timestamps that are not empty
      val time = timestamp ?: return false
      val ind = indicators ?: return false

      if (time.isEmpty()) {
        return false
      }

      val quote = ind.quote ?: return false
      val actuallyQuote = quote.firstOrNull() ?: return false

      val open = actuallyQuote.open ?: return false
      val close = actuallyQuote.close ?: return false
      val high = actuallyQuote.high ?: return false
      val low = actuallyQuote.low ?: return false
      val volume = actuallyQuote.volume ?: return false

      if (open.isEmpty() ||
          close.isEmpty() ||
          high.isEmpty() ||
          low.isEmpty() ||
          volume.isEmpty()) {
        return false
      }

      return true
    }

    @JvmStatic
    @CheckResult
    private fun getIntervalForRange(range: StockChart.IntervalRange): StockChart.IntervalTime {
      return when (range) {
        StockChart.IntervalRange.ONE_DAY -> StockChart.IntervalTime.ONE_MINUTE
        StockChart.IntervalRange.FIVE_DAY -> StockChart.IntervalTime.FIFTEEN_MINUTES
        StockChart.IntervalRange.ONE_MONTH -> StockChart.IntervalTime.SIXTY_MINUTES
        StockChart.IntervalRange.THREE_MONTH -> StockChart.IntervalTime.NINETY_MINUTES
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
