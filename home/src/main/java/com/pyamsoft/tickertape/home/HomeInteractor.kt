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
import com.pyamsoft.tickertape.stocks.api.StockTops
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
      top: StockTops
  ): ResultWrapper<List<TopDataWithChart>> {
    val chartRequests = mutableListOf<Deferred<StockChart>>()
    return try {
      coroutineScope {
        val quotes = top.quotes()
        for (quote in quotes) {
          chartRequests.add(
              async {
                interactor.getChart(
                    force, quote.symbol(), StockChart.IntervalRange.ONE_DAY, includePrePost = false)
              })
        }

        val charts = awaitAll(*chartRequests.toTypedArray())
        return@coroutineScope ResultWrapper.success(
            quotes.map { quote ->
              val chart = charts.firstOrNull { it.symbol() == quote.symbol() }
              TopDataWithChart(
                  title = top.title(),
                  description = top.description(),
                  quote = quote,
                  chart = chart)
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

        val top = interactor.getDayGainers(force, count)
        return@withContext pairWithChart(force, top)
      }

  @CheckResult
  suspend fun getDayLosers(force: Boolean, count: Int): ResultWrapper<List<TopDataWithChart>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val top = interactor.getDayLosers(force, count)
        return@withContext pairWithChart(force, top)
      }
}
