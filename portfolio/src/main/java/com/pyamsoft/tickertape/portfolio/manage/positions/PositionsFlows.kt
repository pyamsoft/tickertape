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

package com.pyamsoft.tickertape.portfolio.manage.positions

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue

data class PositionsViewState(
    val isLoading: Boolean,
    val stock: PositionStock?,
) : UiViewState {

  data class PositionStock
  internal constructor(
      val holding: DbHolding,
      val positions: List<MaybePosition>,
  ) {

    sealed class MaybePosition {
      data class Position internal constructor(val position: DbPosition) : MaybePosition()

      object Header : MaybePosition()

      data class Footer
      internal constructor(
          val totalShares: StockShareValue,
          val averageCost: StockMoneyValue,
          val totalCost: StockMoneyValue
      ) : MaybePosition()
    }
  }
}

sealed class PositionsViewEvent : UiViewEvent {

  object ForceRefresh : PositionsViewEvent()

  data class Remove internal constructor(val index: Int) : PositionsViewEvent()
}

sealed class PositionsControllerEvent : UiControllerEvent
