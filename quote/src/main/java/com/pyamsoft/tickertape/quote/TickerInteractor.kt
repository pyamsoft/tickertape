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
      symbols: List<StockSymbol>,
      options: Options?,
  ): ResultWrapper<List<Ticker>>

  @CheckResult
  suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
      options: Options?,
  ): ResultWrapper<List<Ticker>>

  data class Options(
      val notifyBigMovers: Boolean,
  )

  interface Cache {

    suspend fun invalidateAllQuotes()

    suspend fun invalidateQuotes(symbols: List<StockSymbol>)

    suspend fun invalidateAllCharts()

    suspend fun invalidateCharts(
        symbols: List<StockSymbol>,
        range: StockChart.IntervalRange,
    )
  }
}

@CheckResult
suspend fun TickerInteractor.getWatchListQuotes(
    dao: SymbolQueryDao,
    options: TickerInteractor.Options?,
): ResultWrapper<List<Ticker>> {
  val watchList = dao.query().map { it.symbol }
  return this.getQuotes(
      watchList,
      options,
  )
}
