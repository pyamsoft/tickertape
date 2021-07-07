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

import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.tickertape.portfolio.PortfolioHeader
import com.pyamsoft.tickertape.portfolio.PortfolioViewState
import com.pyamsoft.tickertape.ui.UiDelegate
import javax.inject.Inject

class HomePortfolio @Inject internal constructor(delegate: PortfolioHeader) :
    UiView<HomeViewState, HomeViewEvent>(), UiDelegate {

  private val id by lazy(LazyThreadSafetyMode.NONE) { delegate.id() }

  // This is a weird "kind-of-view-kind-of-delegate". I wonder if this is kosher.
  private val viewBinder: ViewBinder<PortfolioViewState> = createViewBinder(delegate) {}

  init {
    doOnTeardown { viewBinder.teardown() }
  }

  override fun id(): Int {
    return id
  }

  override fun render(state: UiRender<HomeViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: HomeViewState) {
    viewBinder.bindState(
        PortfolioViewState(
            error = state.portfolioError,
            isLoading = state.isLoadingPortfolio,
            portfolio = state.portfolio,
            // Bottom offset is always 0 because the bottom offset is handled by the Home screens
            bottomOffset = 0))
  }
}
