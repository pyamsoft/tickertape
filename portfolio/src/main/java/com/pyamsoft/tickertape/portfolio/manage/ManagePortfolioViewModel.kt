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

import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.tickertape.core.FragmentScope
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import javax.inject.Inject

// Share this single VM between the entire fragment scope, so page is always up to date
@FragmentScope
class ManagePortfolioViewModel
@Inject
internal constructor(
    private val thisHoldingId: DbHolding.Id,
    private val thisSymbol: StockSymbol,
    private val thisType: EquityType,
    private val thisSide: TradeSide,
) :
    UiViewModel<ManagePortfolioViewState, ManagePortfolioControllerEvent>(
        initialState =
            ManagePortfolioViewState(
                symbol = thisSymbol,
                page = DEFAULT_PAGE,
            )) {

  fun handleLoadDefaultPage() {
    loadPage(DEFAULT_PAGE)
  }

  fun handleLoadPositions() {
    loadPage(PortfolioPage.POSITIONS)
  }

  fun handleLoadQuote() {
    loadPage(PortfolioPage.CHART)
  }

  private fun publishPage() {
    return when (state.page) {
      PortfolioPage.POSITIONS -> publish(ManagePortfolioControllerEvent.PushPositions)
      PortfolioPage.CHART -> publish(ManagePortfolioControllerEvent.PushQuote)
    }
  }

  private fun loadPage(page: PortfolioPage) {
    setState(stateChange = { copy(page = page) }, andThen = { publishPage() })
  }

  fun handleOpenAddDialog() {
    publish(
        ManagePortfolioControllerEvent.OpenAdd(
            id = thisHoldingId,
            symbol = thisSymbol,
            type = thisType,
            side = thisSide,
        ))
  }

  companion object {

    private val DEFAULT_PAGE = PortfolioPage.POSITIONS
  }
}
