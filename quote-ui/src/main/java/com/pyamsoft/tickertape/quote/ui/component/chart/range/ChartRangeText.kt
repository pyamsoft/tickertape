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

package com.pyamsoft.tickertape.quote.ui.component.chart.range

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.quote.ui.databinding.ComponentChartRangeItemTextBinding
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.ui.R
import com.pyamsoft.pydroid.ui.R as R2
import javax.inject.Inject

internal class ChartRangeText @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<ChartRangeViewState, ChartRangeViewEvent, ComponentChartRangeItemTextBinding>(
        parent) {

  override val layoutRoot by boundView { componentChartRangeItemText }

  override val viewBinding = ComponentChartRangeItemTextBinding::inflate

  init {
    doOnTeardown { binding.componentChartRangeItemText.text = "" }
  }

  override fun onRender(state: UiRender<ChartRangeViewState>) {
    state.mapChanged { it.range }.render(viewScope) { handleRangeChanged(it) }
    state.mapChanged { it.isSelected }.render(viewScope) { handleSelectedChanged(it) }
  }

  private fun handleSelectedChanged(selected: Boolean) {
    binding.componentChartRangeItemText.setBackgroundResource(if (selected) R2.color.blue500 else 0)
  }

  private fun handleRangeChanged(range: StockChart.IntervalRange) {
    binding.componentChartRangeItemText.text = range.display.uppercase()
  }
}
