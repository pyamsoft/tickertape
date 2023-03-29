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

package com.pyamsoft.tickertape.db.holding

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.DbQuery
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide

interface HoldingQueryDao : DbQuery<DbHolding> {

  @CheckResult suspend fun queryById(id: DbHolding.Id): Maybe<out DbHolding>

  @CheckResult suspend fun queryBySymbol(symbol: StockSymbol): Maybe<out DbHolding>

  @CheckResult
  suspend fun queryByTradeSide(
      symbol: StockSymbol,
      side: TradeSide,
  ): Maybe<out DbHolding>

  interface Cache : DbQuery.Cache {

    suspend fun invalidateByHoldingId(id: DbHolding.Id)

    suspend fun invalidateBySymbol(symbol: StockSymbol)

    suspend fun invalidateByTradeSide(
        symbol: StockSymbol,
        side: TradeSide,
    )
  }
}
