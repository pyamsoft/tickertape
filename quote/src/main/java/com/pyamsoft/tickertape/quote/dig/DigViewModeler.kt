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
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class DigViewModeler<S : MutableDigViewState>
protected constructor(
    private val state: S,
    private val interactor: DigInteractor,
) : AbstractViewModeler<S>(state) {

  @CheckResult
  private fun getLookupSymbol(): StockSymbol {
    return state.lookupSymbol ?: state.ticker.symbol
  }

  protected suspend fun loadStatistics(force: Boolean) {
    val s = state
    interactor
        .getStatistics(
            force = force,
            symbol = getLookupSymbol(),
        )
        .onSuccess { n ->
          s.apply {
            statistics = n
            statisticsError = null
          }
        }
        .onFailure { e ->
          s.apply {
            statistics = null
            statisticsError = e
          }
        }
  }

  protected suspend fun loadNews(force: Boolean) {
    val s = state
    interactor
        .getNews(
            force = force,
            symbol = getLookupSymbol(),
        )
        // Sort news articles by published date
        .map { news ->
          // Run Off main thread
          withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()

            return@withContext news.sortedByDescending { it.publishedAt}
          }
        }
        .onSuccess { n ->
          s.apply {
            news = n
            newsError = null
          }
        }
        .onFailure { e ->
          s.apply {
            news = emptyList()
            newsError = e
          }
        }
  }

  protected suspend fun loadTicker(
      force: Boolean,
  ): ResultWrapper<Ticker> {
    val s = state
    return interactor
        .getChart(
            force = force,
            symbol = s.ticker.symbol,
            range = s.range,
        )
        .onSuccess { t -> s.apply { ticker = t } }
        .onSuccess { ticker ->
          ticker.chart?.also { c ->
            if (c.dates.isEmpty()) {
              Timber.w("No dates, can't pick currentDate and currentPrice")
              return@also
            }

            s.apply {
              onInitialLoad(c)

              // Set the opening price based on the current chart
              openingPrice = c.startingPrice

              // Clear the error on load success
              chartError = null
            }
          }
        }
        .onFailure { Timber.e(it, "Failed to load Ticker") }
        .onFailure { e ->
          s.apply {
            currentPrice = null
            openingPrice = null
            chartError = e
          }
        }
  }

  private fun MutableDigViewState.onInitialLoad(chart: StockChart) {
    if (currentPrice != null) {
      return
    }

    currentDate = chart.currentDate
    currentPrice = chart.currentPrice
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
