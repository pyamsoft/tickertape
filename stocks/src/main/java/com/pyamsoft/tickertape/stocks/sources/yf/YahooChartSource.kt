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
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.InternalApi
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asVolume
import com.pyamsoft.tickertape.stocks.data.StockChartImpl
import com.pyamsoft.tickertape.stocks.network.NetworkChart
import com.pyamsoft.tickertape.stocks.service.ChartService
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooChartSource
@Inject
internal constructor(@InternalApi private val service: ChartService) : ChartSource {

  override suspend fun getCharts(
      force: Boolean,
      symbol: StockSymbol,
      includePrePost: Boolean,
      range: StockChart.IntervalRange
  ): ResultWrapper<List<StockChart>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(fetchCharts(symbol, includePrePost, range))
        } catch (e: Throwable) {
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  private suspend fun fetchCharts(
      symbol: StockSymbol,
      includePrePost: Boolean,
      range: StockChart.IntervalRange
  ): List<StockChart> {
    val interval = getIntervalForRange(range)
    val result =
        service.getQuotes(
            url = YF_QUOTE_SOURCE,
            symbol = symbol.symbol(),
            includePrePost = includePrePost,
            range = range.apiValue,
            interval = interval.apiValue)

    return result
        .chart
        .result
        .asSequence()
        .filterOnlyValidStockData()
        .map { chart ->
          val timestamps = chart.timestamp.requireNotNull()
          val quote = chart.indicators.requireNotNull().first().quote.requireNotNull()
          StockChartImpl(
              symbol = symbol,
              range = range,
              interval = interval,
              dates = timestamps.map { LocalDateTime.from(Instant.ofEpochMilli(it)) },
              volume = quote.volume.requireNotNull().map { it.asVolume() },
              open = quote.open.requireNotNull().map { it.asMoney() },
              close = quote.close.requireNotNull().map { it.asMoney() },
              low = quote.low.requireNotNull().map { it.asMoney() },
              high = quote.high.requireNotNull().map { it.asMoney() },
          )
        }
        .toList()
  }

  companion object {

    private const val YF_QUOTE_SOURCE = "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}"

    @JvmStatic
    @CheckResult
    private fun Sequence<NetworkChart>.filterOnlyValidStockData(): Sequence<NetworkChart> {
      // We need indicators and timestamps that are not empty
      // We need all of these values to have a valid ticker
      return this.filterNot { it.timestamp == null }
          .filterNot { it.indicators == null }
          .filterNot { it.timestamp.requireNotNull().isEmpty() }
          .filterNot { it.indicators.requireNotNull().isEmpty() }
          .filterNot { it.indicators.requireNotNull().firstOrNull() == null }
          .filterNot { it.indicators.requireNotNull().first().quote == null }
          .filterNot { it.indicators.requireNotNull().first().quote?.open == null }
          .filterNot { it.indicators.requireNotNull().first().quote?.close == null }
          .filterNot { it.indicators.requireNotNull().first().quote?.high == null }
          .filterNot { it.indicators.requireNotNull().first().quote?.low == null }
          .filterNot { it.indicators.requireNotNull().first().quote?.volume == null }
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
