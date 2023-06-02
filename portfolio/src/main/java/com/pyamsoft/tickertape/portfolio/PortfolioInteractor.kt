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

package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import kotlinx.coroutines.flow.Flow

interface PortfolioInteractor {

  @CheckResult fun listenForHoldingChanges(): Flow<HoldingChangeEvent>

  @CheckResult fun listenForPositionChanges(): Flow<PositionChangeEvent>

  @CheckResult fun listenForSplitChanges(): Flow<SplitChangeEvent>

  @CheckResult suspend fun getPortfolio(): ResultWrapper<List<PortfolioStock>>

  @CheckResult suspend fun removeHolding(id: DbHolding.Id): ResultWrapper<Boolean>

  @CheckResult
  suspend fun restoreHolding(holding: DbHolding): ResultWrapper<DbInsert.InsertResult<DbHolding>>

  interface Cache {

    suspend fun invalidatePortfolio()
  }
}
