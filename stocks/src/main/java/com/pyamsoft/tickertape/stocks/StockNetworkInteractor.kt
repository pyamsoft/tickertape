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
import com.pyamsoft.tickertape.stocks.scope.StockApi
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.KeyStatisticSource
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import com.pyamsoft.tickertape.stocks.sources.RecommendationSource
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import com.pyamsoft.tickertape.stocks.sources.TopSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StockNetworkInteractor
@Inject
internal constructor(
    @StockApi private val quoteSource: QuoteSource,
    @StockApi private val chartSource: ChartSource,
    @StockApi private val topSource: TopSource,
    @StockApi private val optionsSource: OptionsSource,
    @StockApi private val searchSource: SearchSource,
    @StockApi private val keyStatisticSource: KeyStatisticSource,
    @StockApi private val newsSource: NewsSource,
    @StockApi private val recommendationSource: RecommendationSource,
) : StockInteractor {

  override suspend fun getRecommendations(
      force: Boolean,
      symbol: StockSymbol
  ): StockRecommendations =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext recommendationSource.getRecommendations(force, symbol)
      }

  override suspend fun getKeyStatistics(
      force: Boolean,
      symbols: List<StockSymbol>
  ): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext keyStatisticSource.getKeyStatistics(force, symbols)
      }

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
      symbols: List<StockSymbol>,
  ): List<StockOptions> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext optionsSource.getOptions(force, symbols)
      }

  override suspend fun getTrending(force: Boolean, count: Int): StockTrends =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext topSource.getTrending(force, count)
      }

  override suspend fun getScreener(force: Boolean, screener: StockScreener, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext topSource.getScreener(force, screener, count)
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
        return@withContext chartSource
            .getCharts(force, symbols, range)
            // Remove any duplicated symbols, we expect only one
            .distinctBy { it.symbol }
      }

  override suspend fun resolveOptionLookupIdentifier(
      symbol: StockSymbol,
      expirationDate: LocalDate,
      strikePrice: StockMoneyValue,
      contractType: StockOptions.Contract.Type
  ): String =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()

        return@withContext optionsSource.resolveOptionLookupIdentifier(
            symbol = symbol,
            expirationDate = expirationDate,
            strikePrice = strikePrice,
            contractType = contractType,
        )
      }

  override suspend fun getNews(force: Boolean, symbol: StockSymbol): List<StockNews> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext newsSource.getNews(force, symbol)
      }
}
