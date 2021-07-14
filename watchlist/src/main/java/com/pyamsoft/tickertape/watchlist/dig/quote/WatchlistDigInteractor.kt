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

package com.pyamsoft.tickertape.watchlist.dig.quote

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.quote.QuotedChart
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
  suspend fun getQuoteWithChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange
  ): ResultWrapper<QuotedChart> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          interactor
              .getCharts(force, listOf(symbol), range, includeQuote = true)
              .onFailure { Timber.e(it, "Error getting chart $symbol") }
              .map { it.first() }
        } catch (e: Throwable) {
          Timber.e(e, "Error getting quote with chart ${symbol.symbol()}")
          return@withContext ResultWrapper.failure(e)
        }
      }
}
