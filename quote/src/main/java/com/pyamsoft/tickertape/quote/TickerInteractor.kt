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
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol

interface TickerInteractor {

  @CheckResult
  suspend fun getQuotes(
      force: Boolean,
      symbols: List<StockSymbol>,
  ): ResultWrapper<List<Ticker>>

  @CheckResult
  suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): ResultWrapper<List<Ticker>>
}

@CheckResult
suspend fun TickerInteractor.getWatchListQuotes(
    force: Boolean,
    dao: SymbolQueryDao
): ResultWrapper<List<Ticker>> {
  val watchList = dao.query(force).map { it.symbol() }
  return this.getQuotes(force, watchList)
}
