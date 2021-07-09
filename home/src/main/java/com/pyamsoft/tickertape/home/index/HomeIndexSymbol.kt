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

package com.pyamsoft.tickertape.home.index

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.home.databinding.HomeIndexSymbolBinding
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

class HomeIndexSymbol @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<HomeIndexViewState, Nothing, HomeIndexSymbolBinding>(parent) {

  override val layoutRoot by boundView { homeIndexSymbolRoot }

  override val viewBinding = HomeIndexSymbolBinding::inflate

  init {
    doOnTeardown { binding.homeIndexSymbol.text = "" }

    doOnTeardown { clearCompany() }
  }

  private fun clearCompany() {
    binding.apply {
      homeIndexCompany.text = ""
      homeIndexPrice.text = ""
      homeIndexChange.text = ""
      homeIndexPercent.text = ""
    }
  }

  override fun onRender(state: UiRender<HomeIndexViewState>) {
    state.mapChanged { it.symbol }.render(viewScope) { handleSymbolChanged(it) }
    state.mapChanged { it.quote }.render(viewScope) { handleQuoteChanged(it) }
  }

  private fun handleQuoteChanged(quote: StockQuote?) {
    if (quote == null) {
      clearCompany()
    } else {
      binding.apply {
        homeIndexCompany.text = quote.company().company()

        val session = quote.regular()
        val sign = session.direction().sign()
        homeIndexPrice.text = session.price().asMoneyValue()
        homeIndexChange.text = "${sign}${session.amount().asMoneyValue()}"
        homeIndexPercent.text = "(${sign}${session.percent().asPercentValue()})"

        val color = session.direction().color()
        homeIndexPrice.setTextColor(color)
        homeIndexChange.setTextColor(color)
        homeIndexPercent.setTextColor(color)
      }
    }
  }

  private fun handleSymbolChanged(symbol: StockSymbol) {
    binding.homeIndexSymbol.text = symbol.symbol()
  }
}
