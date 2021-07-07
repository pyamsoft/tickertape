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

package com.pyamsoft.tickertape.stocks

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.MemoryCacheStorage
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class StockInteractorImpl
@Inject
internal constructor(@InternalApi private val interactor: StockInteractor) : StockInteractor {

  private val quotesCache =
      multiCachify<String, List<StockQuote>, List<StockSymbol>>(
          storage = { listOf(MemoryCacheStorage.create(5, TimeUnit.MINUTES)) }) { symbols ->
        interactor.getQuotes(true, symbols)
      }

  private val chartsCache =
      multiCachify<String, StockChart, StockSymbol, Boolean, StockChart.IntervalRange>(
          storage = { listOf(MemoryCacheStorage.create(5, TimeUnit.MINUTES)) }) {
          symbol,
          includePrePost,
          range ->
        interactor.getChart(true, symbol, range, includePrePost)
      }

  override suspend fun getQuotes(force: Boolean, symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val key = getQuoteKey(symbols)
        if (force) {
          quotesCache.key(key).clear()
        }

        return@withContext quotesCache.key(key).call(symbols)
      }

  override suspend fun getChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
      includePrePost: Boolean
  ): StockChart =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val key = getChartKey(symbol, includePrePost, range)
        if (force) {
          chartsCache.key(key).clear()
        }

        return@withContext chartsCache.key(key).call(symbol, includePrePost, range)
      }

  companion object {

    @JvmStatic
    @CheckResult
    private fun getChartKey(
        symbol: StockSymbol,
        includePrePost: Boolean,
        range: StockChart.IntervalRange
    ): String {
      return "${symbol.symbol()}-${includePrePost}-${range.apiValue}"
    }

    @JvmStatic
    @CheckResult
    private fun getQuoteKey(symbols: List<StockSymbol>): String {
      return symbols.map { it.symbol() }.sortedBy { it }.joinToString(",")
    }
  }
}
