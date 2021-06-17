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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.HoldingSummaryBinding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import javax.inject.Inject

class HoldingSummary @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<HoldingViewState, HoldingViewEvent, HoldingSummaryBinding>(parent) {

  override val viewBinding = HoldingSummaryBinding::inflate

  override val layoutRoot by boundView { holdingSummaryRoot }

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<HoldingViewState>) {
    state.mapChanged { it.stock }.mapChanged { it?.totalShares() }.render(viewScope) {
      handleTotalSharesChanged(it)
    }
    state.mapChanged { it.stock }.mapChanged { it?.averagePrice() }.render(viewScope) {
      handleAveragePriceChanged(it)
    }
    state.mapChanged { it.stock }.mapChanged { it?.totalPrice() }.render(viewScope) {
      handleTotalPriceChanged(it)
    }
  }

  private fun clearTotalShares() {
    binding.holdingSummaryTotalSharesText.text = ""
  }

  private fun clearAveragePrice() {
    binding.holdingSummaryAvgPriceText.text = ""
  }

  private fun clearTotalPrice() {
    binding.holdingSummaryTotalPriceText.text = ""
  }

  private fun clear() {
    clearTotalShares()
    clearTotalPrice()
    clearAveragePrice()
  }

  private fun handleTotalSharesChanged(totalShares: StockShareValue?) {
    if (totalShares == null) {
      clearTotalShares()
    } else {
      binding.holdingSummaryTotalSharesText.text = totalShares.asShareValue()
    }
  }

  private fun handleAveragePriceChanged(averagePrice: StockMoneyValue?) {
    if (averagePrice == null) {
      clearAveragePrice()
    } else {
      binding.holdingSummaryAvgPriceText.text = averagePrice.asMoneyValue()
    }
  }

  private fun handleTotalPriceChanged(totalPrice: StockMoneyValue?) {
    if (totalPrice == null) {
      clearTotalPrice()
    } else {
      binding.holdingSummaryTotalPriceText.text = totalPrice.asMoneyValue()
    }
  }
}
