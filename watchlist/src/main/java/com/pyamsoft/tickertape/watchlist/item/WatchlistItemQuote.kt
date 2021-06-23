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

package com.pyamsoft.tickertape.watchlist.item

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.tickertape.quote.QuoteViewDelegate
import com.pyamsoft.tickertape.quote.QuoteViewEvent
import com.pyamsoft.tickertape.quote.QuoteViewState
import javax.inject.Inject

class WatchlistItemQuote @Inject internal constructor(private val delegate: QuoteViewDelegate) :
    UiView<WatchlistItemViewState, WatchlistItemViewEvent>() {

  init {
    doOnInflate {
      delegate.inflate { event ->
        val viewEvent =
            when (event) {
              is QuoteViewEvent.Remove -> WatchlistItemViewEvent.Remove
              is QuoteViewEvent.Select -> WatchlistItemViewEvent.Select
            }
        publish(viewEvent)
      }
    }
  }

  @CheckResult
  internal fun id(): Int {
    return delegate.id()
  }

  override fun render(state: UiRender<WatchlistItemViewState>) {
    state.render(viewScope) { handleQuoteChanged(it) }
  }

  private fun handleQuoteChanged(state: WatchlistItemViewState) {
    delegate.render(
        viewScope, QuoteViewState(symbol = state.symbol, quote = state.quote).asUiRender())
  }
}
