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

package com.pyamsoft.tickertape.home

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockScreener
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class HomeInteractorImpl
@Inject
internal constructor(
    private val tickerInteractor: TickerInteractor,
    private val stockInteractor: StockInteractor,
    private val stockInteractorCache: StockInteractor.Cache,
) : HomeInteractor, HomeInteractor.Cache {

  @CheckResult
  private suspend fun lookupCharts(symbols: List<StockSymbol>): ResultWrapper<List<Ticker>> {
    return tickerInteractor.getCharts(
        symbols,
        CHART_DATE_RANGE,
        options = null,
    )
  }

  override suspend fun invalidateScreener(screener: StockScreener) =
      withContext(context = Dispatchers.IO) { stockInteractorCache.invalidateScreener(screener) }

  override suspend fun getScreener(
      screener: StockScreener,
      count: Int
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        try {
          val top = stockInteractor.getScreener(screener, count)
          lookupCharts(top.quotes.map { it.symbol })
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting screener: $screener")
            ResultWrapper.failure(e)
          }
        }
      }

  @CheckResult
  override suspend fun getTrending(
      count: Int,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        try {
          val trend = stockInteractor.getTrending(count)
          lookupCharts(trend.symbols)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting day trending")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun invalidateTrending() =
      withContext(context = Dispatchers.IO) { stockInteractorCache.invalidateTrending() }

  companion object {

    private val CHART_DATE_RANGE = StockChart.IntervalRange.ONE_DAY
  }
}
