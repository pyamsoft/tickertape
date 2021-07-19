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
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class HomeInteractor
@Inject
internal constructor(private val interactor: StockInteractor) {

  @CheckResult
  private suspend fun pairWithChart(
      force: Boolean,
      quotes: List<StockQuote>,
  ): ResultWrapper<List<TopDataWithChart>> {
    return try {
      coroutineScope {
        val charts =
            interactor.getCharts(
                force, quotes.map { it.symbol() }, StockChart.IntervalRange.ONE_DAY)
        return@coroutineScope ResultWrapper.success(
            quotes.map { quote ->
              val chart = charts.firstOrNull { it.symbol() == quote.symbol() }
              TopDataWithChart(quote = quote, chart = chart)
            })
      }
    } catch (e: Throwable) {
      Timber.e(e, "Error paring charts with tops")
      ResultWrapper.failure(e)
    }
  }

  @CheckResult
  suspend fun getDayGainers(force: Boolean, count: Int): ResultWrapper<List<TopDataWithChart>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val top = interactor.getDayGainers(force, count)
          pairWithChart(force, top.quotes())
        } catch (e: Throwable) {
          Timber.e(e, "Error getting day gainers")
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  suspend fun getDayLosers(force: Boolean, count: Int): ResultWrapper<List<TopDataWithChart>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val top = interactor.getDayLosers(force, count)
          pairWithChart(force, top.quotes())
        } catch (e: Throwable) {
          Timber.e(e, "Error getting day losers")
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  suspend fun getDayTrending(force: Boolean, count: Int): ResultWrapper<List<TopDataWithChart>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val trend = interactor.getTrending(force, count)
          val quotes = interactor.getQuotes(force, trend.symbols())
          pairWithChart(force, quotes)
        } catch (e: Throwable) {
          Timber.e(e, "Error getting day trending")
          ResultWrapper.failure(e)
        }
      }
}
