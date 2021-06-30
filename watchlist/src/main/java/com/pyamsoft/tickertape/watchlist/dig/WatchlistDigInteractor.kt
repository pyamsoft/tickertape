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
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class WatchlistDigInteractor
@Inject
internal constructor(
    private val interactor: QuoteInteractor,
) {

  @CheckResult
  private suspend fun fetchQuote(force: Boolean, symbol: StockSymbol): QuotedStock {
    return interactor
        .getWatchlistQuotes(force)
        .map { quotes ->
          // If this is missing, this will throw, which will wrap into a ResultWrapper.failure
          quotes.first { it.symbol == symbol }
        }
        .getOrThrow()
  }

  @CheckResult
  private suspend fun fetchChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange
  ): ResultWrapper<QuotedChart> {
    return interactor.getWatchlistCharts(force = force, includePrePost = false, range = range)
        .map { quotes ->
          // If this is missing, this will throw, which will wrap into a ResultWrapper.failure
          quotes.first { it.symbol == symbol }
        }
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
          fetchChart(force, symbol, range).map { chart ->
            val quote = fetchQuote(force, symbol)
            QuoteWithChart(quote = quote, chart = chart)
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error getting quote with chart ${symbol.symbol()}")
          ResultWrapper.failure(e)
        }
      }

}
