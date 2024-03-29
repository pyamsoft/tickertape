/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.quote.dig

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.base.BaseTickerInteractorImpl
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockNewsList
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class DigInteractorImpl
protected constructor(
    private val interactor: TickerInteractor,
    private val interactorCache: TickerInteractor.Cache,
    private val stockInteractor: StockInteractor,
    private val stockInteractorCache: StockInteractor.Cache,
) :
    DigInteractor,
    DigInteractor.Cache,
    BaseTickerInteractorImpl(
        stockInteractor,
        stockInteractorCache,
    ) {

  final override suspend fun getRecommendations(
      symbol: StockSymbol
  ): ResultWrapper<StockRecommendations> =
      withContext(context = Dispatchers.Default) {
        try {
          ResultWrapper.success(stockInteractor.getRecommendations(symbol))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting recommendations: ${symbol.raw}")
            ResultWrapper.failure(e)
          }
        }
      }

  final override suspend fun invalidateRecommendations(symbol: StockSymbol) =
      withContext(context = Dispatchers.Default) {
        stockInteractorCache.invalidateRecommendations(symbol)
      }

  final override suspend fun getStatistics(symbol: StockSymbol): ResultWrapper<KeyStatistics> =
      withContext(context = Dispatchers.Default) {
        try {
          ResultWrapper.success(
                  stockInteractor.getKeyStatistics(listOf(symbol)),
              )
              // Only pick out the single quote
              .map { list -> list.first { it.symbol == symbol } }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting statistics: ${symbol.raw}")
            ResultWrapper.failure(e)
          }
        }
      }

  final override suspend fun invalidateStatistics(symbol: StockSymbol) =
      withContext(context = Dispatchers.Default) {
        stockInteractorCache.invalidateStatistics(listOf(symbol))
      }

  final override suspend fun getNews(
      symbol: StockSymbol,
  ): ResultWrapper<StockNewsList> =
      withContext(context = Dispatchers.Default) {
        try {
          ResultWrapper.success(
              stockInteractor
                  .getNews(listOf(symbol))
                  // Just make sure we are only returning news related to this symbol
                  .first { it.symbol == symbol },
          )
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting news ${symbol.raw}")
            ResultWrapper.failure(e)
          }
        }
      }

  final override suspend fun invalidateNews(symbol: StockSymbol) =
      withContext(context = Dispatchers.Default) {
        stockInteractorCache.invalidateNews(listOf(symbol))
      }

  final override suspend fun getChart(
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
  ): ResultWrapper<Ticker> =
      withContext(context = Dispatchers.Default) {
        getCharts(
                symbols = listOf(symbol),
                range = range,
            )
            // Only pick out the single quote
            .map { list -> list.first { it.symbol == symbol } }
      }

  final override suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.Default) {
        try {
          interactor.getCharts(
              symbols = symbols,
              range = range,
              options = null,
          )
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting quotes: $symbols")
            ResultWrapper.failure(e)
          }
        }
      }

  final override suspend fun invalidateChart(
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
  ) = withContext(context = Dispatchers.Default) { invalidateCharts(listOf(symbol), range) }

  final override suspend fun invalidateCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ) =
      withContext(context = Dispatchers.Default) {
        interactorCache.invalidateCharts(symbols, range)
      }
}
