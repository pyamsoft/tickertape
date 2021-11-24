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

package com.pyamsoft.tickertape.home

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class HomeInteractorImpl
@Inject
internal constructor(
    private val tickerInteractor: TickerInteractor,
    private val stockInteractor: StockInteractor,
) : HomeInteractor {

  @CheckResult
  private suspend fun lookupCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
  ): ResultWrapper<List<Ticker>> {
    return tickerInteractor.getCharts(
        force,
        symbols,
        StockChart.IntervalRange.ONE_DAY,
    )
  }

  @CheckResult
  private suspend fun getCharts(
      force: Boolean,
      quotes: List<StockQuote>,
  ): ResultWrapper<List<Ticker>> {
    return lookupCharts(
        force,
        quotes.map { it.symbol() },
    )
  }

  override suspend fun getDayGainers(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val top = stockInteractor.getDayGainers(force, count)
          getCharts(force, top.quotes())
        } catch (e: Throwable) {
          Timber.e(e, "Error getting day gainers")
          ResultWrapper.failure(e)
        }
      }

  override suspend fun getDayLosers(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val top = stockInteractor.getDayLosers(force, count)
          getCharts(force, top.quotes())
        } catch (e: Throwable) {
          Timber.e(e, "Error getting day losers")
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  override suspend fun getDayShorted(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val top = stockInteractor.getMostShorted(force, count)
          getCharts(force, top.quotes())
        } catch (e: Throwable) {
          Timber.e(e, "Error getting most shorted")
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  override suspend fun getDayTrending(
      force: Boolean,
      count: Int,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val trend = stockInteractor.getTrending(force, count)
          lookupCharts(force, trend.symbols())
        } catch (e: Throwable) {
          Timber.e(e, "Error getting day trending")
          ResultWrapper.failure(e)
        }
      }
}
