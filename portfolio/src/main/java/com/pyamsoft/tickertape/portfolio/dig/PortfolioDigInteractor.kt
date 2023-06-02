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

package com.pyamsoft.tickertape.portfolio.dig

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.quote.dig.DigInteractor
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import kotlinx.coroutines.flow.Flow

interface PortfolioDigInteractor : DigInteractor {

  @CheckResult fun watchPositions(): Flow<PositionChangeEvent>

  @CheckResult fun watchSplits(): Flow<SplitChangeEvent>

  @CheckResult suspend fun deletePosition(position: DbPosition): ResultWrapper<Boolean>

  @CheckResult suspend fun deleteSplit(split: DbSplit): ResultWrapper<Boolean>

  @CheckResult suspend fun getSplits(id: DbHolding.Id): ResultWrapper<List<DbSplit>>

  @CheckResult suspend fun getHolding(symbol: StockSymbol): ResultWrapper<Maybe<out DbHolding>>

  @CheckResult suspend fun getPositions(id: DbHolding.Id): ResultWrapper<List<DbPosition>>

  @CheckResult
  suspend fun restorePosition(
      position: DbPosition
  ): ResultWrapper<DbInsert.InsertResult<DbPosition>>

  @CheckResult
  suspend fun restoreSplit(split: DbSplit): ResultWrapper<DbInsert.InsertResult<DbSplit>>

  interface Cache : DigInteractor.Cache {

    suspend fun invalidateSplits(id: DbHolding.Id)

    suspend fun invalidateHolding(symbol: StockSymbol)

    suspend fun invalidatePositions(id: DbHolding.Id)
  }
}
