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
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.tickertape.quote.QuoteViewEvent
import com.pyamsoft.tickertape.quote.QuoteViewState
import com.pyamsoft.tickertape.quote.ui.QuoteViewDelegate
import javax.inject.Inject

class WatchlistItemQuote @Inject internal constructor(delegate: QuoteViewDelegate) :
    UiView<WatchlistItemViewState, WatchlistItemViewEvent>() {

  private val id by lazy(LazyThreadSafetyMode.NONE) { delegate.id() }

  private val viewBinder =
      createViewBinder(delegate) {
        return@createViewBinder when (it) {
          is QuoteViewEvent.Remove -> publish(WatchlistItemViewEvent.Remove)
          is QuoteViewEvent.Select -> publish(WatchlistItemViewEvent.Select)
        }
      }

  init {
    doOnTeardown { viewBinder.teardown() }
  }

  @CheckResult
  internal fun id(): Int {
    return id
  }

  override fun render(state: UiRender<WatchlistItemViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: WatchlistItemViewState) {
    viewBinder.bindState(QuoteViewState(symbol = state.symbol, quote = state.quote, chart = null))
  }
}
