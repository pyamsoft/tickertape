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

package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
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
class TickerInteractorImpl
@Inject
internal constructor(
    private val interactor: StockInteractor,
) : TickerInteractor {

  @CheckResult
  private suspend fun getTickers(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange?,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // If we have no symbols, don't even make the trip
        if (symbols.isEmpty()) {
          return@withContext ResultWrapper.success(emptyList())
        }

        return@withContext try {
          coroutineScope {
            val jobs = mutableListOf<Deferred<Any>>()

            jobs.add(async { interactor.getQuotes(force, symbols) })

            if (range != null) {
              jobs.add(async { interactor.getCharts(force, symbols, range) })
            }

            val result = jobs.awaitAll()
            val (quotes, charts) = parseResult(result)
            val tickers =
                symbols.map { symbol ->
                  val chart = charts.firstOrNull { it.symbol() == symbol }
                  val quote = quotes.firstOrNull { it.symbol() == symbol }
                  return@map Ticker(
                      symbol = symbol,
                      quote = quote,
                      chart = chart,
                  )
                }

            ResultWrapper.success(tickers)
          }
        } catch (e: Exception) {
          Timber.e(e, "Error getting tickers: $symbols $range")
          ResultWrapper.failure(e)
        }
      }

  override suspend fun getQuotes(
      force: Boolean,
      symbols: List<StockSymbol>
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext getTickers(
            force = force,
            symbols = symbols,
            range = null,
        )
      }

  @CheckResult
  override suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext getTickers(
            force = force,
            symbols = symbols,
            range = range,
        )
      }

  companion object {

    @CheckResult
    @Suppress("UNCHECKED_CAST")
    private fun parseResult(result: List<Any>): Pair<List<StockQuote>, List<StockChart>> {
      val quotes = result.first() as List<StockQuote>

      // If more than one, its charts at the end
      val charts =
          if (result.size > 1) {
            result.last() as List<StockChart>
          } else {
            emptyList()
          }

      return quotes to charts
    }
  }
}
