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
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.ui.UiSwipeRefreshContainer
import javax.inject.Inject

class StockChartContainer
@Inject
internal constructor(
    parent: ViewGroup,
    nestedChart: StockChartView,
    nestedCurrent: StockChartCurrent,
    nestedRanges: StockChartRanges
) : UiSwipeRefreshContainer<StockChartViewState, StockChartViewEvent>(parent) {

  init {
    nest(nestedChart, nestedCurrent, nestedRanges)

    doOnInflate {
      binding.containerSwipeRefresh.setOnRefreshListener { publish(StockChartViewEvent.Refresh) }
    }

    doOnTeardown { binding.containerSwipeRefresh.setOnRefreshListener(null) }
  }

  override fun onRender(state: UiRender<StockChartViewState>) {
    state.mapChanged { it.isLoading }.render(viewScope) { handleLoading(it) }
  }
}