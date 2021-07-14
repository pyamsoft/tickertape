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

package com.pyamsoft.tickertape.watchlist.dig.quote

import androidx.annotation.CheckResult
import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.quote.ui.chart.ChartData
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.periodHigh
import com.pyamsoft.tickertape.stocks.api.periodLow
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistDigViewModel
@Inject
internal constructor(interactor: WatchlistDigInteractor, thisSymbol: StockSymbol) :
    UiViewModel<WatchListDigViewState, WatchListDigControllerEvent>(
        initialState =
            WatchListDigViewState(
                symbol = thisSymbol,
                isLoading = false,
                stock = null,
                currentRange = StockChart.IntervalRange.ONE_DAY,
                ranges = emptyList(),
                scrub = null)) {

  private val quoteFetcher =
      highlander<ResultWrapper<QuotedChart>, Boolean, StockChart.IntervalRange> { force, range ->
        interactor.getQuoteWithChart(force, thisSymbol, range)
      }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      val ranges = loadAllRanges()
      setState { copy(ranges = ranges) }
    }
  }

  fun handleRefresh(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchQuote(force) }
  }

  private fun CoroutineScope.fetchQuote(force: Boolean) {
    launch(context = Dispatchers.Default) {
      setState(
          stateChange = { copy(isLoading = true) },
          andThen = { newState ->
            quoteFetcher
                .call(force, newState.currentRange)
                .onSuccess {
                  setState {
                    copy(
                        stock = it.pack(),
                        scrub = latestChartDataFromQuote(it, currentRange),
                        isLoading = false)
                  }
                }
                .onFailure { Timber.e(it, "Failed to fetch quote with stock") }
                .onFailure {
                  setState { copy(stock = it.packError(), scrub = null, isLoading = false) }
                }
          })
    }
  }

  fun handleRangeUpdated(index: Int) {
    val range = state.ranges[index]
    setState(stateChange = { copy(currentRange = range) }, andThen = { fetchQuote(false) })
  }

  fun handleScrub(data: ChartData) {
    setState { copy(scrub = data) }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun latestChartDataFromQuote(
        chart: QuotedChart,
        range: StockChart.IntervalRange
    ): ChartData? {
      val c = chart.chart ?: return null
      return ChartData(
          high = c.periodHigh(),
          low = c.periodLow(),
          baseline = c.startingPrice(),
          range = range,
          date = c.currentDate(),
          price = c.currentPrice())
    }

    @JvmStatic
    @CheckResult
    private fun loadAllRanges(): List<StockChart.IntervalRange> {
      return listOf(
          StockChart.IntervalRange.ONE_DAY,
          StockChart.IntervalRange.FIVE_DAY,
          StockChart.IntervalRange.ONE_MONTH,
          StockChart.IntervalRange.THREE_MONTH,
          StockChart.IntervalRange.SIX_MONTH,
          StockChart.IntervalRange.ONE_YEAR,
          StockChart.IntervalRange.TWO_YEAR,
          StockChart.IntervalRange.FIVE_YEAR,
          StockChart.IntervalRange.TEN_YEAR,
          StockChart.IntervalRange.YTD,
          StockChart.IntervalRange.MAX,
      )
    }
  }
}
