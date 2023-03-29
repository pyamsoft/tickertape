/*
 * Copyright 2023 pyamsoft
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
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.worker.work.bigmover.BigMoverStandalone
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class TickerInteractorImpl
@Inject
internal constructor(
    private val interactor: StockInteractor,
    private val interactorCache: StockInteractor.Cache,
    private val bigMoverStandalone: BigMoverStandalone,
) : TickerInteractor, TickerInteractor.Cache {

  @CheckResult
  private suspend fun getTickers(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange?,
      options: TickerInteractor.Options?,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        // If we have no symbols, don't even make the trip
        if (symbols.isEmpty()) {
          return@withContext ResultWrapper.success(emptyList())
        }

        return@withContext try {
          coroutineScope {
            val jobs =
                mutableListOf<Deferred<Any>>().apply {
                  add(
                      async {
                        try {
                          interactor.getQuotes(symbols)
                        } catch (e: Throwable) {
                          e.ifNotCancellation {
                            Timber.e(e, "Error getting quotes from network: $symbols")
                            emptyList()
                          }
                        }
                      },
                  )
                  if (range != null) {
                    add(
                        async {
                          try {
                            interactor.getCharts(symbols, range)
                          } catch (e: Throwable) {
                            e.ifNotCancellation {
                              Timber.e(e, "Error getting charts from network: $symbols")
                              emptyList()
                            }
                          }
                        },
                    )
                  }
                }

            val result = jobs.awaitAll()

            // Parse result return pair order decides which is which
            val (quotes, charts) = parseResult(result)

            val tickers =
                symbols.map { symbol ->
                  val chart = charts.firstOrNull { it.symbol == symbol }
                  val quote = quotes.firstOrNull { it.symbol == symbol }
                  return@map Ticker(
                      symbol = symbol,
                      quote = quote,
                      chart = chart,
                  )
                }

            handleTickerRefreshSideEffects(tickers, options)

            ResultWrapper.success(tickers)
          }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting tickers: $symbols $range $options")
            ResultWrapper.failure(e)
          }
        }
      }

  private fun CoroutineScope.handleTickerRefreshSideEffects(
      tickers: List<Ticker>,
      options: TickerInteractor.Options?,
  ) {
    if (options == null) {
      return
    }

    if (options.notifyBigMovers) {
      launch(context = Dispatchers.IO) {
        wrapped("Error during big-mover refresh") {
          bigMoverStandalone.notifyBigMovers(
              quotes = tickers.mapNotNull { it.quote },
          )
        }
      }
    }
  }

  override suspend fun getQuotes(
      symbols: List<StockSymbol>,
      options: TickerInteractor.Options?,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        getTickers(
            symbols = symbols,
            range = null,
            options = options,
        )
      }

  override suspend fun invalidateQuotes(symbols: List<StockSymbol>) =
      withContext(context = Dispatchers.IO) { interactorCache.invalidateQuotes(symbols) }

  override suspend fun invalidateAllQuotes() =
      withContext(context = Dispatchers.IO) { interactorCache.invalidateAllQuotes() }

  @CheckResult
  override suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
      options: TickerInteractor.Options?,
  ): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        getTickers(
            symbols = symbols,
            range = range,
            options = options,
        )
      }

  override suspend fun invalidateCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange
  ) = withContext(context = Dispatchers.IO) { interactorCache.invalidateCharts(symbols, range) }

  override suspend fun invalidateAllCharts() =
      withContext(context = Dispatchers.IO) { interactorCache.invalidateAllCharts() }

  companion object {

    private inline fun wrapped(
        errorMessage: String,
        block: () -> Unit,
    ) {
      try {
        block()
      } catch (e: Throwable) {
        e.ifNotCancellation { Timber.e(e, "SIDE-EFFECT: $errorMessage") }
      }
    }

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
