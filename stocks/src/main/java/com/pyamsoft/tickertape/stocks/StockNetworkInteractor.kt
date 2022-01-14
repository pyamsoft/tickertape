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

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockTops
import com.pyamsoft.tickertape.stocks.api.StockTrends
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import com.pyamsoft.tickertape.stocks.sources.TopSource
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Singleton
internal class StockNetworkInteractor
@Inject
internal constructor(
    @InternalApi private val quoteSource: QuoteSource,
    @InternalApi private val chartSource: ChartSource,
    @InternalApi private val topSource: TopSource,
    @InternalApi private val optionsSource: OptionsSource,
    @InternalApi private val searchSource: SearchSource,
) : StockInteractor {

  override suspend fun search(
      force: Boolean,
      query: String,
  ): List<SearchResult> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // When hitting the network upstream, we wait for a couple milliseconds to allow the UI
        // to debounce repeated requests
        // TODO Add variable? Do we ever need to search immediately?
        delay(300L)

        return@withContext searchSource.search(force, query)
      }

  override suspend fun getOptions(
      force: Boolean,
      symbol: StockSymbol,
      date: LocalDateTime?
  ): StockOptions =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext optionsSource.getOptions(force, symbol, date)
      }

  override suspend fun getTrending(force: Boolean, count: Int): StockTrends =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext topSource.getTrending(force, count)
      }

  override suspend fun getDayGainers(force: Boolean, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext topSource.getDayGainers(force, count)
      }

  override suspend fun getDayLosers(force: Boolean, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext topSource.getDayLosers(force, count)
      }

  override suspend fun getMostShorted(force: Boolean, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext topSource.getMostShorted(force, count)
      }

  override suspend fun getQuotes(force: Boolean, symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext quoteSource.getQuotes(force, symbols)
      }

  override suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): List<StockChart> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext chartSource.getCharts(force, symbols, range)
            // Remove any duplicated symbols, we expect only one
            .distinctBy { it.symbol() }
      }
}
