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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.tickertape.quote.QuoteViewDelegate
import com.pyamsoft.tickertape.quote.QuoteViewEvent
import com.pyamsoft.tickertape.quote.QuoteViewState
import com.pyamsoft.tickertape.quote.QuotedStock
import javax.inject.Inject

class HoldingQuote @Inject internal constructor(private val delegate: QuoteViewDelegate) :
    UiView<PortfolioListViewState, PortfolioListViewEvent>() {

  init {
    doOnInflate {
      delegate.inflate { event ->
        val viewEvent =
            when (event) {
              is QuoteViewEvent.Remove -> PortfolioListViewEvent.Remove
              is QuoteViewEvent.Select -> PortfolioListViewEvent.Select
            }
        publish(viewEvent)
      }
    }
  }

  @CheckResult
  fun id(): Int {
    return delegate.id()
  }

  override fun render(state: UiRender<PortfolioListViewState>) {
    state.mapChanged { it.stock }.mapChanged { it.quote }.render(viewScope) {
      handleQuoteChanged(it)
    }
  }

  private fun handleQuoteChanged(quote: QuotedStock?) {
    if (quote != null) {
      delegate.render(
          viewScope, QuoteViewState(symbol = quote.symbol, quote = quote.quote).asUiRender())
    }
  }
}
