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

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockTops
import com.pyamsoft.tickertape.stocks.api.StockTrends
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class StockInteractorImpl
@Inject
internal constructor(
    @InternalApi private val stockCache: StockCache,
    @InternalApi private val interactor: StockInteractor,
) : StockInteractor {

  private val optionsCache =
      multiCachify<OptionsKey, StockOptions, StockSymbol, LocalDateTime?> { symbol, date ->
        interactor.getOptions(true, symbol, date)
      }

  private val trendingCache =
      cachify<StockTrends, Int>(storage = { listOf(createNewMemoryCacheStorage()) }) {
        interactor.getTrending(true, it)
      }

  private val gainerCache =
      cachify<StockTops, Int>(storage = { listOf(createNewMemoryCacheStorage()) }) {
        interactor.getDayGainers(true, it)
      }

  private val loserCache =
      cachify<StockTops, Int>(storage = { listOf(createNewMemoryCacheStorage()) }) {
        interactor.getDayLosers(true, it)
      }

  override suspend fun getTrending(force: Boolean, count: Int): StockTrends =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          trendingCache.clear()
        }

        return@withContext trendingCache.call(count)
      }

  override suspend fun getDayGainers(force: Boolean, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          gainerCache.clear()
        }

        return@withContext gainerCache.call(count)
      }

  override suspend fun getDayLosers(force: Boolean, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          loserCache.clear()
        }

        return@withContext loserCache.call(count)
      }

  override suspend fun getOptions(
      force: Boolean,
      symbol: StockSymbol,
      date: LocalDateTime?
  ): StockOptions =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val key = OptionsKey(symbol, date)
        if (force) {
          optionsCache.key(key).clear()
        }

        return@withContext optionsCache.key(key).call(symbol, date)
      }

  override suspend fun getQuotes(force: Boolean, symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          symbols.forEach { stockCache.removeQuote(it) }
        }

        return@withContext stockCache.getQuotes(symbols) { interactor.getQuotes(force, it) }
      }

  override suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): List<StockChart> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          symbols.forEach { stockCache.removeChart(it, range) }
        }

        return@withContext stockCache.getCharts(symbols, range) { s, r ->
          interactor.getCharts(force, s, r)
        }
      }

  private data class OptionsKey(val symbol: StockSymbol, val date: LocalDateTime?)
}
