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

package com.pyamsoft.tickertape.quote.ui.chart

import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal class QuoteChartViewModel
@AssistedInject
internal constructor(@Assisted savedState: UiSavedState, thisSymbol: StockSymbol) :
    UiSavedStateViewModel<QuoteChartViewState, QuoteChartControllerEvent>(
        savedState,
        initialState =
            QuoteChartViewState(
                currentScrub = null,
                range = StockChart.IntervalRange.ONE_DAY,
                quote = null,
                chart = null,
                symbol = thisSymbol)) {

  fun handleUpdateQuoteWithChart(symbol: StockSymbol, quote: StockQuote?, chart: StockChart?) {
    setState { copy(symbol = symbol, quote = quote, chart = chart) }
  }

  fun handleRangeUpdated(range: StockChart.IntervalRange) {
    setState { copy(range = range) }
  }

  fun handleScrubUpdated(scrub: ChartData) {
    setState { copy(currentScrub = scrub) }
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<QuoteChartViewModel> {
    override fun create(savedState: UiSavedState): QuoteChartViewModel
  }
}