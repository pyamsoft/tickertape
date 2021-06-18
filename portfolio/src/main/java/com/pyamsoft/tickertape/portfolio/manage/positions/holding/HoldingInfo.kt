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

package com.pyamsoft.tickertape.portfolio.manage.positions.holding

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.HoldingInfoBinding
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

class HoldingInfo @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<HoldingViewState, HoldingViewEvent, HoldingInfoBinding>(parent) {

  override val viewBinding = HoldingInfoBinding::inflate

  override val layoutRoot by boundView { positionHoldingRoot }

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<HoldingViewState>) {
    state.mapChanged { it.stock }.mapChanged { it?.quote }.mapChanged { it?.symbol }.render(
        viewScope) { handleSymbolChanged(it) }

    state.mapChanged { it.stock }.mapChanged { it?.quote }.mapChanged { it?.quote }.render(
        viewScope) { handleQuoteChanged(it) }
  }

  private fun clear() {
    binding.positionHoldingSymbol.text = ""
    binding.positionHoldingCompany.text = ""
  }

  private fun handleSymbolChanged(stock: StockSymbol?) {
    if (stock == null) {
      binding.positionHoldingSymbol.text = ""
    } else {
      binding.positionHoldingSymbol.text = stock.symbol()
    }
  }

  private fun handleQuoteChanged(stock: StockQuote?) {
    if (stock == null) {
      binding.positionHoldingCompany.text = ""
    } else {
      binding.positionHoldingCompany.text = stock.company().company()
    }
  }
}
