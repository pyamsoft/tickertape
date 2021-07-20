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

package com.pyamsoft.tickertape.main.add.result

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.main.databinding.SymbolResultNameBinding
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

class SymbolResultName @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<SearchResultViewState, SearchResultViewEvent, SymbolResultNameBinding>(parent) {

  override val layoutRoot by boundView { symbolResultRoot }

  override val viewBinding = SymbolResultNameBinding::inflate

  init {
    doOnTeardown {
      binding.apply {
        symbolResultName.text = null
        symbolResultSymbol.text = null
      }
    }
  }

  override fun onRender(state: UiRender<SearchResultViewState>) {
    state.mapChanged { it.result }.apply {
      mapChanged { it.name() }.render(viewScope) { handleNameChanged(it) }
      mapChanged { it.symbol() }.render(viewScope) { handleSymbolChanged(it) }
    }
  }

  private fun handleSymbolChanged(symbol: StockSymbol) {
    binding.symbolResultSymbol.text = symbol.symbol()
  }

  private fun handleNameChanged(company: StockCompany) {
    binding.symbolResultName.text = company.company()
  }
}
