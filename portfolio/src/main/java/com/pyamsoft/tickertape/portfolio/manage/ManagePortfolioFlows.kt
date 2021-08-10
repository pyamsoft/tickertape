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

package com.pyamsoft.tickertape.portfolio.manage

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide

data class ManagePortfolioViewState
internal constructor(
    val symbol: StockSymbol,
    val page: PortfolioPage,
) : UiViewState

sealed class ManagePortfolioViewEvent : UiViewEvent {

  object Close : ManagePortfolioViewEvent()

  object Add : ManagePortfolioViewEvent()

  object OpenPositions : ManagePortfolioViewEvent()

  object OpenQuote : ManagePortfolioViewEvent()
}

sealed class ManagePortfolioControllerEvent : UiControllerEvent {

  data class OpenAdd
  internal constructor(
      val id: DbHolding.Id,
      val symbol: StockSymbol,
      val type: EquityType,
      val side: TradeSide,
  ) : ManagePortfolioControllerEvent()

  object PushPositions : ManagePortfolioControllerEvent()

  object PushQuote : ManagePortfolioControllerEvent()
}
