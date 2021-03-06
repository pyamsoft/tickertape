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

package com.pyamsoft.tickertape.quote.dig

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class DigInteractorImpl
protected constructor(
    private val interactor: TickerInteractor,
    private val stockInteractor: StockInteractor,
) : DigInteractor {

  final override suspend fun getRecommendations(
      force: Boolean,
      symbol: StockSymbol,
  ): ResultWrapper<StockRecommendations> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(
              stockInteractor.getRecommendations(
                  force = force,
                  symbol = symbol,
              ))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting recommendations: ${symbol.raw}")
            ResultWrapper.failure(e)
          }
        }
      }

  final override suspend fun getStatistics(
      force: Boolean,
      symbol: StockSymbol,
  ): ResultWrapper<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(
                  stockInteractor.getKeyStatistics(
                      force = force,
                      symbols = listOf(symbol),
                  ),
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

  final override suspend fun getNews(
      force: Boolean,
      symbol: StockSymbol,
  ): ResultWrapper<List<StockNews>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(stockInteractor.getNews(force, symbol))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting news ${symbol.raw}")
            ResultWrapper.failure(e)
          }
        }
      }

  final override suspend fun getChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
  ): ResultWrapper<Ticker> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext getCharts(
                force = force,
                symbols = listOf(symbol),
                range = range,
            )
            // Only pick out the single quote
            .map { list -> list.first { it.symbol == symbol } }
      }

  override suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          interactor.getCharts(
              force = force,
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
}
