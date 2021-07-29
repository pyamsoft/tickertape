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

package com.pyamsoft.tickertape.portfolio.item

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.ui.view.QuoteView
import com.pyamsoft.tickertape.quote.ui.view.QuoteViewState
import com.pyamsoft.tickertape.ui.UiDelegate
import javax.inject.Inject

class PortfolioItemQuote @Inject internal constructor(parent: ViewGroup) :
    QuoteView<PortfolioItemViewState.Item, PortfolioItemViewEvent>(parent), UiDelegate {

  override fun handleRemove() {
    publish(PortfolioItemViewEvent.Remove)
  }

  override fun handleSelect() {
    publish(PortfolioItemViewEvent.Select)
  }

  override fun onRender(state: UiRender<PortfolioItemViewState.Item>) {
    state.mapChanged { it.stock }.render(viewScope) { handleStockChanged(it) }
  }

  private fun handleStockChanged(stock: PortfolioStock) {
    handleRender(
        QuoteViewState(symbol = stock.holding.symbol(), quote = stock.quote?.quote, chart = null)
            .asUiRender())
  }
}
