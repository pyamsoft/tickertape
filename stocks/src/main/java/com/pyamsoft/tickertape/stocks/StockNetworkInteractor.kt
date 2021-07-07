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

package com.pyamsoft.tickertape.stocks

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class StockNetworkInteractor
@Inject
internal constructor(
    @InternalApi private val quoteSource: QuoteSource,
    @InternalApi private val chartSource: ChartSource,
) : StockInteractor {

  override suspend fun getQuotes(force: Boolean, symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext quoteSource.getQuotes(force, symbols)
      }

  override suspend fun getChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
      includePrePost: Boolean
  ): StockChart =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext chartSource.getChart(force, symbol, range, includePrePost)
      }
}
