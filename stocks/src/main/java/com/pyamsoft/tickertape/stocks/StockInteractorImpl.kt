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
import com.pyamsoft.tickertape.stocks.cache.NewsCache
import com.pyamsoft.tickertape.stocks.cache.OptionsCache
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import com.pyamsoft.tickertape.stocks.scope.InternalStockApi
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class StockInteractorImpl
@Inject
internal constructor(
    @InternalStockApi private val stockCache: StockCache,
    @InternalStockApi private val optionsCache: OptionsCache,
    @InternalStockApi private val statisticsCache: KeyStatisticsCache,
    @InternalStockApi private val newsCache: NewsCache,
    @InternalStockApi private val interactor: StockInteractor,
) : StockInteractor, StockInteractor.Cache {

  private val trendingCache =
      cachify<StockTrends, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) {
        interactor.getTrending(it)
      }

  private val topCaches =
      multiCachify<StockScreener, StockTops, StockScreener, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { screener, count ->
        interactor.getScreener(screener, count)
      }

  private val searchCache =
      multiCachify<String, List<SearchResult>, String>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) {
        interactor.search(it)
      }

  private val recommendationCache =
      multiCachify<StockSymbol, StockRecommendations, StockSymbol>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) {
        interactor.getRecommendations(it)
      }

  override suspend fun getKeyStatistics(symbols: List<StockSymbol>): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext statisticsCache.getStatistics(symbols) {
          interactor.getKeyStatistics(it)
        }
      }

  override suspend fun invalidateStatistics(symbols: List<StockSymbol>) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        symbols.forEach { statisticsCache.removeStatistics(it) }
      }

  override suspend fun search(query: String): List<SearchResult> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext searchCache.key(query).call(query)
      }

  override suspend fun invalidateSearch(query: String) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        // Remove the cache from the search map and clear it if it exists
        searchCache.key(query).clear()
      }

  override suspend fun getTrending(count: Int): StockTrends =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext trendingCache.call(count)
      }

  override suspend fun invalidateTrending() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        trendingCache.clear()
      }

  override suspend fun getScreener(screener: StockScreener, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext topCaches.key(screener).call(screener, count)
      }

  override suspend fun invalidateScreener(screener: StockScreener) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        topCaches.key(screener).clear()
      }

  override suspend fun getOptions(symbols: List<StockSymbol>): List<StockOptions> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext optionsCache.getOptions(symbols) { interactor.getOptions(it) }
      }

  override suspend fun invalidateOptions(symbols: List<StockSymbol>) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        symbols.forEach { optionsCache.removeOption(it) }
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

  override suspend fun getQuotes(symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext stockCache.getQuotes(symbols) { interactor.getQuotes(it) }
      }

  override suspend fun invalidateQuotes(symbols: List<StockSymbol>) {
    withContext(context = Dispatchers.IO) {
      Enforcer.assertOffMainThread()
      symbols.forEach { stockCache.removeQuote(it) }
    }
  }

  override suspend fun invalidateAllQuotes() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        stockCache.removeAllQuotes()
      }

  override suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): List<StockChart> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext stockCache.getCharts(symbols, range) { s, r ->
          interactor.getCharts(s, r)
        }
      }

  override suspend fun invalidateCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange
  ) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        symbols.forEach { stockCache.removeChart(it, range) }
      }

  override suspend fun invalidateAllCharts() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        stockCache.removeAllCharts()
      }

  override suspend fun getNews(symbols: List<StockSymbol>): List<StockNews> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext newsCache.getNews(symbols) { interactor.getNews(it) }
      }

  override suspend fun invalidateNews(symbols: List<StockSymbol>) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        symbols.forEach { newsCache.removeNews(it) }
      }

  override suspend fun getRecommendations(symbol: StockSymbol): StockRecommendations =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext recommendationCache.key(symbol).call(symbol)
      }

  override suspend fun invalidateRecommendations(symbol: StockSymbol) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        recommendationCache.key(symbol).clear()
      }
}
