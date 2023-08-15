/*
 * Copyright 2023 pyamsoft
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
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.DeleteRestoreViewModeler
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.chart.ChartData
import com.pyamsoft.tickertape.quote.chart.ChartDataProcessor
import com.pyamsoft.tickertape.quote.dig.recommend.StockRec
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockNewsList
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class DigViewModeler<S : MutableDigViewState>
protected constructor(
    override val state: S,
    private val lookupSymbol: StockSymbol?,
    private val processor: ChartDataProcessor,
    private val interactor: DigInteractor,
    private val interactorCache: DigInteractor.Cache,
) : DeleteRestoreViewModeler<S>(state) {

  private var optionsLookupJob: Job? = null
  private var statisticsLookupJob: Job? = null
  private var newsLookupJob: Job? = null
  private var chartLookupJob: Job? = null
  private var recommendationLookupJob: Job? = null
  private var recommendationChartJob: Job? = null

  @CheckResult
  private fun StockSymbol.asTicker(): Ticker {
    return Ticker(
        symbol = this,
        quote = null,
        chart = null,
    )
  }

  @CheckResult
  private fun getLookupSymbol(): StockSymbol {
    return lookupSymbol ?: state.ticker.value.symbol
  }

  @CheckResult
  protected suspend fun CoroutineScope.loadOptionsChainAsync(
      force: Boolean
  ): Deferred<ResultWrapper<StockOptions>> {
    val scope = this
    val s = state
    val symbol = getLookupSymbol()

    // Reset options if this is the first time call
    if (s.optionsChain.value == null) {
      s.optionsExpirationDate.value = null
      s.optionsSection.value = StockOptions.Contract.Type.CALL
    }

    optionsLookupJob?.cancel()
    return scope
        .async(context = Dispatchers.Default) {
          if (force) {
            interactorCache.invalidateOptionsChain(symbol)
          }

          interactor
              .getOptionsChain(symbol, s.optionsExpirationDate.value)
              .onSuccess { o ->
                s.apply {
                  optionsChain.value = o
                  optionsError.value = null

                  // Set the expiration date to the first one
                  if (s.optionsExpirationDate.value == null) {
                    s.optionsExpirationDate.value = o.expirationDates.firstOrNull()
                  }
                }
              }
              .onFailure { Timber.e(it, "Failed to load options chain for $symbol") }
              .onFailure { e ->
                s.apply {
                  optionsChain.value = null
                  optionsError.value = e
                }
              }
        }
        .also { optionsLookupJob = it }
  }

  private suspend fun CoroutineScope.loadQuotesForRecommendations(
      recommendations: StockRecommendations
  ) {
    val scope = this

    recommendationChartJob?.cancel()
    recommendationChartJob =
        scope.launch(context = Dispatchers.Default) {
          interactor
              .getCharts(
                  symbols = recommendations.recommendations,
                  range = StockChart.IntervalRange.ONE_DAY,
              )
              .onSuccess { rec ->
                Timber.d("Loaded full recs for symbols: $rec")
                state.recommendations.value =
                    rec.filter { it.chart != null }
                        .map { t ->
                          StockRec(
                              ticker = t,
                              chart = processor.processChartEntries(t.chart.requireNotNull()),
                          )
                        }
              }
              .onFailure { e ->
                Timber.e(e, "Failed to load full ticker quote for recs: $recommendations")
              }
        }
  }

  @CheckResult
  protected suspend fun CoroutineScope.loadRecommendationsAsync(
      force: Boolean
  ): Deferred<ResultWrapper<StockRecommendations>> {
    val scope = this
    val s = state
    val symbol = getLookupSymbol()

    recommendationLookupJob?.cancel()
    return scope
        .async(context = Dispatchers.Default) {
          if (force) {
            interactorCache.invalidateRecommendations(symbol)
          }
          interactor
              .getRecommendations(symbol)
              .onFailure { Timber.e(it, "Failed to load recommendations for $symbol") }
              .onFailure { e ->
                s.apply {
                  recommendations.value = emptyList()
                  recommendationError.value = e
                }
              }
              .onSuccess { loadQuotesForRecommendations(it) }
        }
        .also { recommendationLookupJob = it }
  }

  @CheckResult
  protected suspend fun CoroutineScope.loadStatisticsAsync(
      force: Boolean
  ): Deferred<ResultWrapper<KeyStatistics>> {
    val scope = this

    val s = state
    val symbol = getLookupSymbol()

    statisticsLookupJob?.cancel()
    return scope
        .async(context = Dispatchers.Default) {
          if (force) {
            interactorCache.invalidateStatistics(symbol)
          }
          interactor
              .getStatistics(symbol)
              .onSuccess { n ->
                s.apply {
                  statistics.value = n
                  statisticsError.value = null
                }
              }
              .onFailure { e ->
                s.apply {
                  statistics.value = null
                  statisticsError.value = e
                }
              }
        }
        .also { statisticsLookupJob = it }
  }

  @CheckResult
  protected suspend fun CoroutineScope.loadNewsAsync(
      force: Boolean
  ): Deferred<ResultWrapper<StockNewsList>> {
    val scope = this

    val s = state
    val symbol = getLookupSymbol()

    newsLookupJob?.cancel()
    return scope
        .async(context = Dispatchers.Default) {
          if (force) {
            interactorCache.invalidateNews(symbol)
          }
          interactor
              .getNews(symbol)
              .onSuccess { n ->
                s.apply {
                  news.value = n.news
                  newsError.value = null
                }
              }
              .onFailure { e ->
                s.apply {
                  news.value = emptyList()
                  newsError.value = e
                }
              }
        }
        .also { newsLookupJob = it }
  }

  @CheckResult
  protected suspend fun CoroutineScope.loadTickerAsync(
      force: Boolean
  ): Deferred<ResultWrapper<Ticker>> {
    val scope = this

    val s = state
    val symbol = s.ticker.value.symbol
    val range = s.range

    chartLookupJob?.cancel()
    return scope
        .async(context = Dispatchers.Default) {
          if (force) {
            interactorCache.invalidateChart(symbol, range.value)
          }

          return@async interactor
              .getChart(symbol, range.value)
              .onSuccess { s.ticker.value = it }
              .onSuccess { ticker ->
                ticker.chart?.also { c ->
                  if (c.dates.isEmpty()) {
                    Timber.w("No dates, can't pick currentDate and currentPrice")
                    return@also
                  }

                  s.apply {
                    onInitialLoad(c)

                    // Set the opening price based on the current chart
                    openingPrice.value = c.startingPrice

                    // Clear the error on load success
                    chartError.value = null

                    // Process chart values for drawing
                    chart.value = processor.processChartEntries(c)
                  }
                }
              }
              .onFailure { Timber.e(it, "Failed to load Ticker") }
              .onFailure { e ->
                s.apply {
                  currentPrice.value = null
                  openingPrice.value = null
                  chartError.value = e
                }
              }
        }
        .also { chartLookupJob = it }
  }

  private fun MutableDigViewState.onInitialLoad(chart: StockChart) {
    if (currentPrice.value != null) {
      return
    }

    currentDate.value = chart.currentDate
    currentPrice.value = chart.currentPrice
  }

  fun handleOptionsSectionChanged(section: StockOptions.Contract.Type) {
    state.optionsSection.value = section
  }

  fun handleOptionsExpirationDateChanged(scope: CoroutineScope, date: LocalDate) {
    state.optionsExpirationDate.value = date

    // Reload options
    scope.launch(context = Dispatchers.Default) {
      Timber.d("Reload options chain on exp date changed")
      loadOptionsChainAsync(false).await()
    }
  }

  fun dispose() {
    newsLookupJob?.cancel()
    newsLookupJob = null

    statisticsLookupJob?.cancel()
    statisticsLookupJob = null

    chartLookupJob?.cancel()
    chartLookupJob = null

    optionsLookupJob?.cancel()
    optionsLookupJob = null

    recommendationChartJob?.cancel()
    recommendationChartJob = null

    recommendationLookupJob?.cancel()
    recommendationLookupJob = null

    onDispose()
  }

  fun handleChartRangeSelected(
      scope: CoroutineScope,
      range: StockChart.IntervalRange,
  ) {
    val oldRange = state.range.value
    if (oldRange == range) {
      return
    }

    state.range.value = range
    handleLoadTicker(scope = scope, force = true)
  }

  fun handleChartDateScrubbed(data: ChartData) {
    state.apply {
      currentDate.value = data.date
      currentPrice.value = data.price
    }
  }

  protected abstract fun onDispose()

  abstract fun handleLoadTicker(
      scope: CoroutineScope,
      force: Boolean,
  )
}
