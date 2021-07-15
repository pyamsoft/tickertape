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

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.quote.ui.chart.ChartData
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.PackedData

data class StockChartViewState
internal constructor(
    val symbol: StockSymbol,
    val isLoading: Boolean,
    val stock: PackedData<QuotedChart>?,
    val currentRange: StockChart.IntervalRange,
    val ranges: List<StockChart.IntervalRange>,
    val scrub: ChartData?
) : UiViewState

sealed class StockChartViewEvent : UiViewEvent {

  object Refresh : StockChartViewEvent()

  data class RangeUpdated internal constructor(val index: Int) : StockChartViewEvent()

  data class Scrub internal constructor(val data: ChartData) : StockChartViewEvent()
}

sealed class StockChartControllerEvent : UiControllerEvent
