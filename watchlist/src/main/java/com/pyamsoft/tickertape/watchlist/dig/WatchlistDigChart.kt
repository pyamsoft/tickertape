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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.tickertape.quote.ui.chart.ChartData
import com.pyamsoft.tickertape.quote.ui.chart.QuoteChartView
import com.pyamsoft.tickertape.quote.ui.chart.QuoteChartViewState
import javax.inject.Inject

class WatchlistDigChart @Inject internal constructor(parent: ViewGroup) :
    QuoteChartView<WatchListDigViewState, WatchListDigViewEvent>(parent) {

  init {
    doOnTeardown { binding.quoteChart.scrubListener = null }
    doOnInflate {
      binding.quoteChart.apply {
        isScrubEnabled = true

        // Setup scrub listener
        setScrubListener { raw ->
          val data = raw as? ChartData
          if (data == null) {
            clearScrubView()
          } else {
            handleScrubbedView(data)
          }
        }
      }
    }
  }

  private fun clearScrubView() {}

  private fun handleScrubbedView(data: ChartData) {}

  override fun onRender(state: UiRender<WatchListDigViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: WatchListDigViewState) {
    val symbol = state.symbol
    val stock = state.stock
    handleRender(
        (QuoteChartViewState(symbol = symbol, quote = stock?.quote, chart = stock?.chart)
            .asUiRender()))
  }
}
