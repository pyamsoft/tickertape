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

import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.StockChart
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistDigViewModeler
@Inject
internal constructor(
    private val state: MutableWatchlistDigViewState,
    interactor: WatchlistDigInteractor,
) : AbstractViewModeler<WatchlistDigViewState>(state) {

  private val loadRunner =
      highlander<ResultWrapper<Ticker>, Boolean> { force ->
        interactor.getChart(
            force = force,
            symbol = state.ticker.symbol,
            range = state.range,
        )
      }

  fun handleLoadTicker(
      scope: CoroutineScope,
      force: Boolean,
  ) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      loadRunner
          .call(force)
          .onSuccess { t ->
            state.apply {
              ticker = t
              error = null
            }
          }
          .onFailure { Timber.e(it, "Failed to load Ticker") }
          .onFailure { e ->
            state.apply {
              // Don't need to reset ticker here since whatever we have at this point is valid
              // enough, just set error
              error = e
            }
          }
          .onFinally { state.isLoading = false }
    }
  }

  fun handleRangeSelected(scope: CoroutineScope, range: StockChart.IntervalRange) {
    val oldRange = state.range
    if (oldRange == range) {
      return
    }

    state.range = range
    handleLoadTicker(scope = scope, force = true)
  }

  fun handleDateScrubbed(scope: CoroutineScope, data: Chart.Data?) {
    if (data == null) {
      return
    }
  }
}
