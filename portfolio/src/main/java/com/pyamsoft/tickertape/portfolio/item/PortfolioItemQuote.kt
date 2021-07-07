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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.QuoteViewEvent
import com.pyamsoft.tickertape.quote.QuoteViewState
import com.pyamsoft.tickertape.quote.ui.QuoteViewDelegate
import javax.inject.Inject

class PortfolioItemQuote @Inject internal constructor(delegate: QuoteViewDelegate) :
    UiView<PortfolioItemViewState, PortfolioItemViewEvent>() {

  private val id by lazy(LazyThreadSafetyMode.NONE) { delegate.id() }

  private val viewBinder =
      createViewBinder(delegate) {
        return@createViewBinder when (it) {
          is QuoteViewEvent.Remove -> publish(PortfolioItemViewEvent.Remove)
          is QuoteViewEvent.Select -> publish(PortfolioItemViewEvent.Select)
        }
      }

  init {
    doOnTeardown { viewBinder.teardown() }
  }

  @CheckResult
  internal fun id(): Int {
    return id
  }

  override fun render(state: UiRender<PortfolioItemViewState>) {
    state.mapChanged { it.stock }.render(viewScope) { handleStockChanged(it) }
  }

  private fun handleStockChanged(stock: PortfolioStock) {
    viewBinder.bindState(
        QuoteViewState(symbol = stock.holding.symbol(), quote = stock.quote?.quote, chart = null))
  }
}
