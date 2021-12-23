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

package com.pyamsoft.tickertape.portfolio.dig

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.quote.dig.DigInteractor

interface PortfolioDigInteractor : DigInteractor {

  @CheckResult suspend fun watchPositions(onEvent: (PositionChangeEvent) -> Unit)

  @CheckResult suspend fun deletePositon(position: DbPosition): ResultWrapper<Boolean>

  @CheckResult
  suspend fun getHolding(
      force: Boolean,
      id: DbHolding.Id,
  ): ResultWrapper<DbHolding>

  @CheckResult
  suspend fun getPositions(
      force: Boolean,
      id: DbHolding.Id,
  ): ResultWrapper<List<DbPosition>>
}
