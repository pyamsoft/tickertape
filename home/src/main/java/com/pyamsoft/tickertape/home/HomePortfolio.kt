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
import com.pyamsoft.tickertape.portfolio.PortfolioViewState
import javax.inject.Inject

class HomePortfolio @Inject internal constructor(parent: ViewGroup) :
    BasePortfolioHeader<HomeViewState>(parent) {

  override fun onRender(state: UiRender<HomeViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: HomeViewState) {
    handleRender(
        PortfolioViewState(
                isLoading = state.isLoadingPortfolio,
                portfolio = state.portfolio,
                // Bottom offset is always 0 because the bottom offset is handled by the Home
                // screens
                bottomOffset = 0)
            .asUiRender())
  }
}