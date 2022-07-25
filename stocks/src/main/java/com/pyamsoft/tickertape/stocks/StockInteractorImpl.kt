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
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockScreener
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockTops
import com.pyamsoft.tickertape.stocks.api.StockTrends
import com.pyamsoft.tickertape.stocks.cache.KeyStatisticsCache
import com.pyamsoft.tickertape.stocks.cache.OptionsCache
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import com.pyamsoft.tickertape.stocks.scope.InternalStockApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StockInteractorImpl
@Inject
internal constructor(
    @InternalStockApi private val stockCache: StockCache,
    @InternalStockApi private val optionsCache: OptionsCache,
    @InternalStockApi private val statisticsCache: KeyStatisticsCache,
    @InternalStockApi private val interactor: StockInteractor,
) : StockInteractor {

  private val trendingCache =
      cachify<StockTrends, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.getTrending(true, it) }

  private val topCaches =
      multiCachify<StockScreener, StockTops, StockScreener, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { screener, count -> interactor.getScreener(true, screener, count) }

  private val searchCache =
      multiCachify<String, List<SearchResult>, String>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.search(true, it) }

  private val newsCache =
      multiCachify<StockSymbol, List<StockNews>, StockSymbol>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.getNews(true, it) }

  private val recommendationCache =
      multiCachify<StockSymbol, StockRecommendations, StockSymbol>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { interactor.getRecommendations(true, it) }

  override suspend fun getKeyStatistics(
      force: Boolean,
      symbols: List<StockSymbol>
  ): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          symbols.forEach { statisticsCache.removeStatistics(it) }
        }

        return@withContext statisticsCache.getStatistics(symbols) {
          interactor.getKeyStatistics(true, it)
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

  override suspend fun getScreener(force: Boolean, screener: StockScreener, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val cache = topCaches.key(screener)
        if (force) {
          cache.clear()
        }

        return@withContext cache.call(screener, count)
      }

  override suspend fun getOptions(
      force: Boolean,
      symbols: List<StockSymbol>,
  ): List<StockOptions> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          symbols.forEach { optionsCache.removeOption(it) }
        }

        return@withContext optionsCache.getOptions(symbols) { interactor.getOptions(true, it) }
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

        return@withContext stockCache.getQuotes(symbols) { interactor.getQuotes(true, it) }
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
          interactor.getCharts(true, s, r)
        }
      }

  override suspend fun getNews(
      force: Boolean,
      symbol: StockSymbol,
  ): List<StockNews> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          newsCache.key(symbol).clear()
        }

        return@withContext newsCache.key(symbol).call(symbol)
      }

  override suspend fun getRecommendations(
      force: Boolean,
      symbol: StockSymbol
  ): StockRecommendations =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          recommendationCache.key(symbol).clear()
        }

        return@withContext recommendationCache.key(symbol).call(symbol)
      }
}
