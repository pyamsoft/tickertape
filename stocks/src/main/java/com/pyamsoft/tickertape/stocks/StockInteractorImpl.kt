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

package com.pyamsoft.tickertape.stocks

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockNewsList
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    @InternalStockApi private val newsCache: NewsCache,
    @InternalStockApi private val interactor: StockInteractor,
) : StockInteractor, StockInteractor.Cache {

  private val trendingCache =
      multiCachify<Int, StockTrends, Int>(
          storage = { listOf(createNewMemoryCacheStorage()) },
      ) { count ->
        interactor.getTrending(count)
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

  @CheckResult
  private suspend fun getJustKeyStatistics(symbols: List<StockSymbol>): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        // Key statistics but without a Quote pairing
        statisticsCache.getStatistics(symbols) { interactor.getKeyStatistics(it) }
      }

  override suspend fun getKeyStatistics(symbols: List<StockSymbol>): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        // Fetch stats and quote at the same time
        val jobs =
            mutableListOf<Deferred<*>>().apply {
              // Get the stats
              add(async { getJustKeyStatistics(symbols) })

              // Get the quotes
              add(async { getQuotes(symbols) })
            }

        val result = jobs.awaitAll()

        // We can assume this based on job add order
        @Suppress("UNCHECKED_CAST")
        val stats: List<KeyStatistics> = result[0] as List<KeyStatistics>

        // We can assume this based on job add order
        @Suppress("UNCHECKED_CAST") val quotes: List<StockQuote> = result[1] as List<StockQuote>

        return@withContext stats.map { stat ->
          // Find the quote that pairs with this stat
          val quote = quotes.find { q -> q.symbol == stat.symbol }

          // Pair it up
          return@map stat.withQuote(quote)
        }
      }

  override suspend fun invalidateStatistics(symbols: List<StockSymbol>) =
      withContext(context = Dispatchers.IO) {
        symbols.forEach { statisticsCache.removeStatistics(it) }
      }

  override suspend fun search(query: String): List<SearchResult> =
      withContext(context = Dispatchers.IO) { searchCache.key(query).call(query) }

  override suspend fun invalidateSearch(query: String) =
      withContext(context = Dispatchers.IO) {
        // Remove the cache from the search map and clear it if it exists
        searchCache.key(query).clear()
      }

  override suspend fun getTrending(count: Int): StockTrends =
      withContext(context = Dispatchers.IO) { trendingCache.key(count).call(count) }

  override suspend fun invalidateTrending() =
      withContext(context = Dispatchers.IO) { trendingCache.clear() }

  override suspend fun getScreener(screener: StockScreener, count: Int): StockTops =
      withContext(context = Dispatchers.IO) { topCaches.key(screener).call(screener, count) }

  override suspend fun invalidateScreener(screener: StockScreener) =
      withContext(context = Dispatchers.IO) { topCaches.key(screener).clear() }

  override suspend fun getOptions(
      symbols: List<StockSymbol>,
      expirationDate: LocalDate?,
  ): List<StockOptions> =
      withContext(context = Dispatchers.IO) {
        optionsCache.getOptions(symbols, expirationDate) { s, e -> interactor.getOptions(s, e) }
      }

  override suspend fun invalidateOptions(
      symbols: List<StockSymbol>,
      expirationDate: LocalDate?,
  ) =
      withContext(context = Dispatchers.IO) {
        symbols.forEach { optionsCache.removeOption(it, expirationDate) }
      }

  override suspend fun resolveOptionLookupIdentifier(
      symbol: StockSymbol,
      expirationDate: LocalDate,
      strikePrice: StockMoneyValue,
      contractType: StockOptions.Contract.Type
  ): String =
      withContext(context = Dispatchers.Default) {
        interactor.resolveOptionLookupIdentifier(
            symbol = symbol,
            expirationDate = expirationDate,
            strikePrice = strikePrice,
            contractType = contractType,
        )
      }

  override suspend fun getQuotes(symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        stockCache.getQuotes(symbols) { interactor.getQuotes(it) }
      }

  override suspend fun invalidateQuotes(symbols: List<StockSymbol>) {
    withContext(context = Dispatchers.IO) { symbols.forEach { stockCache.removeQuote(it) } }
  }

  override suspend fun invalidateAllQuotes() =
      withContext(context = Dispatchers.IO) { stockCache.removeAllQuotes() }

  override suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): List<StockChart> =
      withContext(context = Dispatchers.IO) {
        stockCache.getCharts(symbols, range) { s, r -> interactor.getCharts(s, r) }
      }

  override suspend fun invalidateCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange
  ) =
      withContext(context = Dispatchers.IO) {
        symbols.forEach { stockCache.removeChart(it, range) }
      }

  override suspend fun invalidateAllCharts() =
      withContext(context = Dispatchers.IO) { stockCache.removeAllCharts() }

  override suspend fun getNews(symbols: List<StockSymbol>): List<StockNewsList> =
      withContext(context = Dispatchers.IO) {
        newsCache.getNews(symbols) { interactor.getNews(it) }
      }

  override suspend fun invalidateNews(symbols: List<StockSymbol>) =
      withContext(context = Dispatchers.IO) { symbols.forEach { newsCache.removeNews(it) } }

  override suspend fun getRecommendations(symbol: StockSymbol): StockRecommendations =
      withContext(context = Dispatchers.IO) { recommendationCache.key(symbol).call(symbol) }

  override suspend fun invalidateRecommendations(symbol: StockSymbol) =
      withContext(context = Dispatchers.IO) { recommendationCache.key(symbol).clear() }
}
