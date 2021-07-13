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
import androidx.core.view.updateLayoutParams
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.quote.ui.chart.QuoteChartView
import com.pyamsoft.tickertape.quote.ui.chart.QuoteChartViewState
import javax.inject.Inject

class HomeIndexChart @Inject internal constructor(parent: ViewGroup) :
    QuoteChartView<HomeIndexViewState, Nothing>(parent) {

  init {
    doOnInflate {
      binding.quoteChartRoot.updateLayoutParams { this.height = 120.asDp(layoutRoot.context) }
    }
  }

  override fun onRender(state: UiRender<HomeIndexViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: HomeIndexViewState) {
    handleRender(
        (QuoteChartViewState(symbol = state.symbol, quote = state.quote, chart = state.chart)
            .asUiRender()))
  }
}
