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
import javax.inject.Inject

// Share this single VM between the entire fragment scope, so page is always up to date
@FragmentScope
class ManagePortfolioViewModel @Inject internal constructor() :
    UiViewModel<ManagePortfolioViewState, ManagePortfolioControllerEvent>(
        initialState = ManagePortfolioViewState(page = DEFAULT_PAGE)) {

  fun handleLoadDefaultPage() {
    loadPage(DEFAULT_PAGE)
  }

  private fun publishPage() {
    return when (state.page) {
      PortfolioPage.HOLDING -> publish(ManagePortfolioControllerEvent.PushHolding)
      PortfolioPage.POSITIONS -> publish(ManagePortfolioControllerEvent.PushPositions)
    }
  }

  private fun loadPage(page: PortfolioPage) {
    setState(stateChange = { copy(page = page) }, andThen = { publishPage() })
  }

  fun handleLoadPositionsPage() {
    loadPage(PortfolioPage.POSITIONS)
  }

  companion object {

    private val DEFAULT_PAGE = PortfolioPage.HOLDING
  }
}