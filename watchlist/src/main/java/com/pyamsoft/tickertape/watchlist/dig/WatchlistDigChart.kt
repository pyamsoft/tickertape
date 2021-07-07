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

package com.pyamsoft.tickertape.watchlist.dig

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.tickertape.quote.ui.chart.QuoteChartView
import com.pyamsoft.tickertape.quote.ui.chart.QuoteChartViewState
import javax.inject.Inject

class WatchlistDigChart @Inject internal constructor(delegate: QuoteChartView) :
    UiView<WatchListDigViewState, WatchListDigViewEvent>() {

  private val id by lazy(LazyThreadSafetyMode.NONE) { delegate.id() }

  // This is a weird "kind-of-view-kind-of-delegate". I wonder if this is kosher.
  private val viewBinder: ViewBinder<QuoteChartViewState> = createViewBinder(delegate) {}

  init {
    doOnTeardown { viewBinder.teardown() }
  }

  @CheckResult
  fun id(): Int {
    return id
  }

  override fun render(state: UiRender<WatchListDigViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: WatchListDigViewState) {
    val symbol = state.symbol
    val stock = state.stock
    viewBinder.bindState(
        QuoteChartViewState(symbol = symbol, quote = stock?.quote, chart = stock?.chart))
  }
}
