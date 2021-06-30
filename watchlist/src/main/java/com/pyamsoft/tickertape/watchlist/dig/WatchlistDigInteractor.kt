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

package com.pyamsoft.tickertape.watchlist.dig

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class WatchlistDigInteractor
@Inject
internal constructor(
    private val interactor: QuoteInteractor,
) {

  @CheckResult
  private suspend fun fetchQuote(force: Boolean, symbol: StockSymbol): QuotedStock? {
    return interactor
        .getWatchlistQuotes(force)
        .map { quotes -> quotes.first { it.symbol == symbol } }
        .onFailure { Timber.e(it, "Error getting watchlist quotes $symbol") }
        .getOrNull()
  }

  @CheckResult
  private suspend fun fetchChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange
  ): QuotedChart? {
    return interactor
        .getChart(force, symbol, range, includePrePost = false)
        .onFailure { Timber.e(it, "Error getting chart $symbol") }
        .getOrNull()
  }

  @CheckResult
  suspend fun getQuoteWithChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange
  ): ResultWrapper<QuoteWithChart> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          coroutineScope {
            val quoteJob = async { fetchQuote(force, symbol) }
            val chartJob = async { fetchChart(force, symbol, range) }

            // Run in parallel
            val jobs = awaitAll(quoteJob, chartJob)

            // Pull these out since we know what types they should be
            // as instead of as? since we need the cast to succeed
            // but the type it casts to can be nullable
            val quote = jobs[0] as QuotedStock?
            val chart = jobs[1] as QuotedChart?

            return@coroutineScope ResultWrapper.success(
                QuoteWithChart(symbol, quote?.quote, chart?.chart))
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error getting quote with chart ${symbol.symbol()}")
          return@withContext ResultWrapper.failure(e)
        }
      }
}
