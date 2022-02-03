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

import com.pyamsoft.cachify.Cached2
import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockTops
import com.pyamsoft.tickertape.stocks.api.StockTrends
import com.pyamsoft.tickertape.stocks.cache.KeyStatisticsCache
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import com.pyamsoft.tickertape.stocks.scope.InternalStockApi
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
internal class StockInteractorImpl
@Inject
internal constructor(
    @InternalStockApi private val stockCache: StockCache,
    @InternalStockApi private val statisticsCache: KeyStatisticsCache,
    @InternalStockApi private val interactor: StockInteractor,
) : StockInteractor {

  private val optionsMutex = Mutex()

  private val optionsCache =
      mutableMapOf<OptionsKey, Cached2<StockOptions, StockSymbol, LocalDate?>>()

  private val trendingCache =
      cachify<StockTrends, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.getTrending(true, it) }

  private val gainerCache =
      cachify<StockTops, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.getDayGainers(true, it) }

  private val loserCache =
      cachify<StockTops, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.getDayLosers(true, it) }

  private val shortedCache =
      cachify<StockTops, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.getMostShorted(true, it) }

  private val searchCache =
      multiCachify<String, List<SearchResult>, String>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.search(true, it) }

  override suspend fun getKeyStatistics(
      force: Boolean,
      symbols: List<StockSymbol>
  ): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          symbols.forEach { statisticsCache.removeStatistics(it) }
        }

        return@withContext statisticsCache.getStatistics(symbols) { s ->
          interactor.getKeyStatistics(force, s)
        }
      }

  override suspend fun search(
      force: Boolean,
      query: String,
  ): List<SearchResult> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          // Remove the cache from the search map and clear it if it exists
          searchCache.key(query).clear()
        }

        return@withContext searchCache.key(query).call(query)
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

  override suspend fun getMostShorted(force: Boolean, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          shortedCache.clear()
        }

        return@withContext shortedCache.call(count)
      }

  override suspend fun getOptions(
      force: Boolean,
      symbol: StockSymbol,
      date: LocalDate?
  ): StockOptions =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext optionsMutex.withLock {
          val key = OptionsKey(symbol, date)
          if (force) {
            // Remove the cache from the options map and clear it if it exists
            optionsCache.remove(key)?.clear()
          }

          val cached = optionsCache[key]
          val cache: Cached2<StockOptions, StockSymbol, LocalDate?>
          if (cached == null) {
            cache =
                cachify<StockOptions, StockSymbol, LocalDate?>(
                    storage = { listOf(createNewMemoryCacheStorage()) },
                ) { s, d -> interactor.getOptions(true, s, d) }
                    .also { c -> optionsCache[key] = c }
          } else {
            cache = cached
          }

          return@withLock cache.call(symbol, date)
        }
      }

  override suspend fun resolveOptionLookupIdentifier(
      symbol: StockSymbol,
      expirationDate: LocalDate,
      strikePrice: StockMoneyValue,
      contractType: StockOptions.Contract.Type
  ): String =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()

        return@withContext interactor.resolveOptionLookupIdentifier(
            symbol = symbol,
            expirationDate = expirationDate,
            strikePrice = strikePrice,
            contractType = contractType,
        )
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

  private data class OptionsKey(val symbol: StockSymbol, val date: LocalDate?)
}
