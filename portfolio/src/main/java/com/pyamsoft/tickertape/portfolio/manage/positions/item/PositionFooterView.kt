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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.FooterBinding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import javax.inject.Inject

class PositionFooterView @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PositionItemViewState.Footer, Nothing, FooterBinding>(parent) {

  override val viewBinding = FooterBinding::inflate

  override val layoutRoot by boundView { positionFooter }

  init {
    doOnTeardown {
      binding.apply {
        positionFooterAvgText.text = ""
        positionFooterPositionText.text = ""
        positionFooterTotalSharesText.text = ""
      }
    }
  }

  override fun onRender(state: UiRender<PositionItemViewState.Footer>) {
    state.mapChanged { it.isOption }.render(viewScope) { handleOptionChanged(it) }
    state.mapChanged { it.totalShares }.render(viewScope) { handleShareCountChanged(it) }
    state.mapChanged { it.totalCost }.render(viewScope) { handleTotalPriceChanged(it) }
    state.mapChanged { it.averageCost }.render(viewScope) { handleAverageChanged(it) }
  }

  private fun handleOptionChanged(isOption: Boolean) {
    binding.positionFooterTotalSharesLabel.text = "Total ${if (isOption) "Contracts" else "Shares"}"
  }

  private fun handleAverageChanged(price: StockMoneyValue) {
    binding.positionFooterAvgText.text = price.asMoneyValue()
  }

  private fun handleShareCountChanged(shareCount: StockShareValue) {
    binding.positionFooterTotalSharesText.text = shareCount.asShareValue()
  }

  private fun handleTotalPriceChanged(price: StockMoneyValue) {
    binding.positionFooterPositionText.text = price.asMoneyValue()
  }
}
