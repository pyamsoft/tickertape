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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.tickertape.quote.ui.view.QuoteView
import com.pyamsoft.tickertape.quote.ui.view.QuoteViewState
import javax.inject.Inject

class WatchlistItemQuote @Inject internal constructor(parent: ViewGroup) :
    QuoteView<WatchlistItemViewState, WatchlistItemViewEvent>(parent) {

  override fun handleRemove() {
    publish(WatchlistItemViewEvent.Remove)
  }

  override fun handleSelect() {
    publish(WatchlistItemViewEvent.Select)
  }

  override fun onRender(state: UiRender<WatchlistItemViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: WatchlistItemViewState) {
    handleRender(
        QuoteViewState(symbol = state.symbol, quote = state.quote, chart = null).asUiRender())
  }
}
