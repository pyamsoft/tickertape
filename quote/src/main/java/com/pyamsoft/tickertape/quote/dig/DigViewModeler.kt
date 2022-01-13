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

package com.pyamsoft.tickertape.quote.dig

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.StockChart
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

abstract class DigViewModeler<S : MutableDigViewState>
protected constructor(
    private val state: S,
    private val interactor: DigInteractor,
) : AbstractViewModeler<S>(state) {

  @CheckResult
  protected suspend fun onLoadTicker(
      force: Boolean,
  ): ResultWrapper<Ticker> =
      interactor
          .getChart(
              force = force,
              symbol = state.ticker.symbol,
              range = state.range,
          )
          .onSuccess { t -> state.apply { ticker = t } }
          .onSuccess { ticker ->
            ticker.chart?.also { c ->
              if (c.dates().isEmpty()) {
                Timber.w("No dates, can't pick currentDate and currentPrice")
                return@also
              }

              state.onInitialLoad(c)
            }
          }
          .onFailure { Timber.e(it, "Failed to load Ticker") }
          .onFailure {
            state.apply {
              currentPrice = null
              mostRecentPrice = null
            }
          }

  private fun MutableDigViewState.onInitialLoad(chart: StockChart) {
    if (currentPrice == null) {
      currentDate = chart.currentDate()
      currentPrice = chart.currentPrice()

      // Set the most recent price as the initial load current price (should basically be today's most recent quote)
      mostRecentPrice = currentPrice
    }
  }

  fun handleRangeSelected(
      scope: CoroutineScope,
      range: StockChart.IntervalRange,
  ) {
    val oldRange = state.range
    if (oldRange == range) {
      return
    }

    state.range = range
    handleLoadTicker(scope = scope, force = true)
  }

  fun handleDateScrubbed(data: Chart.Data?) {
    if (data == null) {
      return
    }

    state.apply {
      currentDate = data.date
      currentPrice = data.price
    }
  }

  abstract fun handleLoadTicker(
      scope: CoroutineScope,
      force: Boolean,
  )
}
