/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.stocks.cache

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol

interface StockCache {

  suspend fun removeQuote(symbol: StockSymbol)

  suspend fun removeAllQuotes()

  @CheckResult
  suspend fun getQuotes(
      symbols: List<StockSymbol>,
      resolve: suspend (List<StockSymbol>) -> List<StockQuote>
  ): List<StockQuote>

  suspend fun removeChart(symbol: StockSymbol, range: StockChart.IntervalRange)

  suspend fun removeAllCharts()

  @CheckResult
  suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
      resolve: suspend (List<StockSymbol>, StockChart.IntervalRange) -> List<StockChart>
  ): List<StockChart>
}
