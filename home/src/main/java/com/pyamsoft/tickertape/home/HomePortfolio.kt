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

package com.pyamsoft.tickertape.home

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.tickertape.portfolio.BasePortfolioHeader
import com.pyamsoft.tickertape.ui.TabsSection
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.portfolio.PortfolioViewState
import javax.inject.Inject

class HomePortfolio @Inject internal constructor(parent: ViewGroup) :
    BasePortfolioHeader<HomeViewState>(parent) {

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.portfolioState }.render(viewScope) { handlePortfolioChanged(it) }
  }

  private fun handlePortfolioChanged(state: HomeViewState.RenderState<PortfolioStockList>) {
    handleRender(
        PortfolioViewState(
                isLoading = state.isLoading,
                portfolio = state.data,
                // Always show stocks section
                section = TabsSection.STOCKS,
                // Bottom offset is always 0 because the bottom offset is handled by the Home
                // screens
                bottomOffset = 0)
            .asUiRender())
  }
}
