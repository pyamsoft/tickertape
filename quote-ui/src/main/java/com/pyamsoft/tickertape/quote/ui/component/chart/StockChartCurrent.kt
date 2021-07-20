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

package com.pyamsoft.tickertape.quote.ui.component.chart

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.ui.chart.ChartData
import com.pyamsoft.tickertape.quote.ui.databinding.ComponentChartCurrentBinding
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.DATE_TIME_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockChart
import javax.inject.Inject

internal class StockChartCurrent @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<StockChartViewState, StockChartViewEvent, ComponentChartCurrentBinding>(parent) {

  override val layoutRoot by boundView { componentChartCurrent }

  override val viewBinding = ComponentChartCurrentBinding::inflate

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<StockChartViewState>) {
    state.mapChanged { it.scrub }.render(viewScope) { handleScrubChanged(it) }
  }

  private fun clear() {
    binding.apply {
      componentChartCurrentDate.text = null
      componentChartCurrentPrice.text = null
    }
  }

  private fun handleScrubChanged(data: ChartData?) {
    if (data == null) {
      clear()
    } else {
      binding.apply {
        val formatter =
            if (data.range < StockChart.IntervalRange.THREE_MONTH) DATE_TIME_FORMATTER
            else DATE_FORMATTER
        componentChartCurrentDate.text = formatter.get().requireNotNull().format(data.date)
        componentChartCurrentPrice.text = data.price.asMoneyValue()
      }
    }
  }
}
