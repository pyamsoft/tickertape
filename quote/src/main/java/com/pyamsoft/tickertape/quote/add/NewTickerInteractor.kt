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

package com.pyamsoft.tickertape.quote.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.base.BaseTickerInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import java.time.LocalDate

interface NewTickerInteractor : BaseTickerInteractor {

  @CheckResult suspend fun search(query: String): ResultWrapper<List<SearchResult>>

  @CheckResult suspend fun resolveTicker(symbol: StockSymbol): ResultWrapper<Ticker>

  @CheckResult
  suspend fun resolveOptionsIdentifier(
      symbol: StockSymbol,
      expirationDate: LocalDate,
      strikePrice: StockMoneyValue,
      contractType: StockOptions.Contract.Type,
  ): String

  @CheckResult
  suspend fun insertNewTicker(
      symbol: StockSymbol,
      equityType: EquityType,
      tradeSide: TradeSide,
  ): ResultWrapper<DbInsert.InsertResult<StockSymbol>>

  interface Cache : BaseTickerInteractor.Cache {

    suspend fun invalidateSearch(query: String)

    suspend fun invalidateTicker(symbol: StockSymbol)
  }
}
