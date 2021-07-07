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
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
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
class QuoteInteractor
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val interactor: StockInteractor
) {

  // TODO Same code as in WatchlistInteractor, common somehow?
  @CheckResult
  private suspend fun getSymbols(force: Boolean): List<StockSymbol> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext symbolQueryDao.query(force).map { it.symbol() }
      }

  @CheckResult
  suspend fun getWatchlistQuotes(force: Boolean): ResultWrapper<List<QuotedStock>> =
      withContext(context = Dispatchers.IO) {
        val symbols = getSymbols(force)
        return@withContext getQuotes(force, symbols)
      }

  @CheckResult
  suspend fun getQuotes(
      force: Boolean,
      symbols: List<StockSymbol>
  ): ResultWrapper<List<QuotedStock>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // If we have no symbols, don't even make the trip
        if (symbols.isEmpty()) {
          return@withContext ResultWrapper.success(emptyList())
        }

        return@withContext try {
          val quotes = interactor.getQuotes(force, symbols)
          ResultWrapper.success(
              symbols.map { s ->
                QuotedStock(symbol = s, quote = quotes.firstOrNull { it.symbol() == s })
              })
        } catch (e: Throwable) {
          Timber.e(e, "Error getting quotes: $symbols")
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  private suspend fun fetchChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
      includePrePost: Boolean,
      includeQuote: Boolean
  ): QuotedChart = coroutineScope {
    val deferred = mutableListOf<Deferred<*>>()
    deferred.add(async { interactor.getChart(force, symbol, range, includePrePost) })

    if (includeQuote) {
      deferred.add(async { interactor.getQuotes(force, listOf(symbol)).first() })
    }

    val results = awaitAll(*deferred.toTypedArray())

    val chart = results[0] as StockChart?
    val quote = if (includeQuote) results[1] as StockQuote? else null

    return@coroutineScope QuotedChart(symbol = symbol, chart = chart, quote = quote)
  }

  @CheckResult
  suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
      includePrePost: Boolean,
      includeQuote: Boolean
  ): ResultWrapper<List<QuotedChart>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          coroutineScope {
            val deferreds = mutableListOf<Deferred<QuotedChart>>()
            for (symbol in symbols) {
              deferreds.add(
                  async { fetchChart(force, symbol, range, includePrePost, includeQuote) })
            }

            val charts = awaitAll(*deferreds.toTypedArray())
            return@coroutineScope ResultWrapper.success(charts)
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error getting charts")
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  suspend fun getChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
      includePrePost: Boolean,
      includeQuote: Boolean,
  ): ResultWrapper<QuotedChart> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext getCharts(force, listOf(symbol), range, includePrePost, includeQuote)
            .map { it.first() }
      }
}
