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

package com.pyamsoft.tickertape.portfolio.manage.positions.item

import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue

sealed class PositionItemViewState : UiViewState {

  data class Position internal constructor(val holding: DbHolding, val position: DbPosition) :
      PositionItemViewState()

  object Header : PositionItemViewState()

  data class Footer
  internal constructor(
      val totalShares: StockShareValue,
      val averageCost: StockMoneyValue,
      val totalCost: StockMoneyValue
  ) : PositionItemViewState()
}

sealed class PositionItemViewEvent : UiViewEvent {

  object Remove : PositionItemViewEvent()
}
