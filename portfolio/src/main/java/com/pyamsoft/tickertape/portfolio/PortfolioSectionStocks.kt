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

package com.pyamsoft.tickertape.portfolio

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioSectionStocksBinding
import javax.inject.Inject

class PortfolioSectionStocks @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PortfolioViewState, PortfolioViewEvent, PortfolioSectionStocksBinding>(parent) {

  override val viewBinding = PortfolioSectionStocksBinding::inflate

  override val layoutRoot by boundView { portfolioSectionStocks }

  override fun onRender(state: UiRender<PortfolioViewState>) {
    state.mapChanged { it.section }.render(viewScope) { handleSection(it) }
  }

  private fun handleSection(section: PortfolioSection) {
    binding.portfolioSectionStocks.isVisible = section == PortfolioSection.STOCKS
  }
}
