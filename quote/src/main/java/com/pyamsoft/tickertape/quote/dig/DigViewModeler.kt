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
import com.pyamsoft.highlander.highlander
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate

abstract class DigViewModeler<S : MutableDigViewState>
protected constructor(
    override val state: S,
    private val lookupSymbol: StockSymbol?,
    private val processor: ChartDataProcessor,
    interactor: DigInteractor,
    interactorCache: DigInteractor.Cache,
) : DeleteRestoreViewModeler<S>(state) {

  private val optionsRunner =
      highlander<ResultWrapper<StockOptions>, Boolean, StockSymbol, LocalDate?> {
          force,
          symbol,
          expirationDate ->
        if (force) {
          interactorCache.invalidateOptionsChain(symbol)
        }
        return@highlander interactor.getOptionsChain(symbol, expirationDate)
      }

  private val statisticsRunner =
      highlander<ResultWrapper<KeyStatistics>, Boolean, StockSymbol> { force, symbol ->
        if (force) {
          interactorCache.invalidateStatistics(symbol)
        }
        return@highlander interactor.getStatistics(symbol)
      }

  private val recommendationRunner =
      highlander<ResultWrapper<StockRecommendations>, Boolean, StockSymbol> { force, symbol ->
        if (force) {
          interactorCache.invalidateRecommendations(symbol)
        }
        return@highlander interactor.getRecommendations(symbol)
      }

  private val newsRunner =
      highlander<ResultWrapper<StockNewsList>, Boolean, StockSymbol> { force, symbol ->
        if (force) {
          interactorCache.invalidateNews(symbol)
        }

        return@highlander interactor
            .getNews(symbol)
            // Sort news articles by published date
            .map { news ->
              // Run Off main thread
              return@map withContext(context = Dispatchers.IO) {
                news.sortedByDescending { it.publishedAt }
              }
            }
      }

  private val chartRunner =
      highlander<ResultWrapper<Ticker>, Boolean, StockSymbol, StockChart.IntervalRange> {
          force,
          symbol,
          range ->
        if (force) {
          interactorCache.invalidateChart(symbol, range)
        }

        return@highlander interactor.getChart(symbol, range)
      }

  private val recommendationLookupRunner =
      highlander<ResultWrapper<List<Ticker>>, StockRecommendations> { recs ->
        interactor.getCharts(
            symbols = recs.recommendations,
            range = StockChart.IntervalRange.ONE_DAY,
        )
      }

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

  protected suspend fun loadOptionsChain(force: Boolean) {
    val s = state
    val symbol = getLookupSymbol()

    // Reset options if this is the first time call
    if (s.optionsChain.value == null) {
      s.optionsExpirationDate.value = null
      s.optionsSection.value = StockOptions.Contract.Type.CALL
    }

    optionsRunner
        .call(force, symbol, s.optionsExpirationDate.value)
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

  protected suspend fun loadRecommendations(force: Boolean) {
    val s = state
    val symbol = getLookupSymbol()
    recommendationRunner
        .call(force, symbol)
        .onFailure { Timber.e(it, "Failed to load recommendations for $symbol") }
        .onFailure { e ->
          s.apply {
            recommendations.value = emptyList()
            recommendationError.value = e
          }
        }
        .onSuccess { loadQuotesForRecommendations(it) }
  }

  private suspend fun loadQuotesForRecommendations(recommendations: StockRecommendations) =
      coroutineScope {
        launch(context = Dispatchers.Main) {
          recommendationLookupRunner
              .call(recommendations)
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

  protected suspend fun loadStatistics(force: Boolean) {
    val s = state
    val symbol = getLookupSymbol()
    statisticsRunner
        .call(force, symbol)
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

  protected suspend fun loadNews(force: Boolean) {
    val s = state
    val symbol = getLookupSymbol()
    newsRunner
        .call(force, symbol)
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

  protected suspend fun loadTicker(
      force: Boolean,
  ): ResultWrapper<Ticker> {
    val s = state
    val symbol = s.ticker.value.symbol
    val range = s.range
    return chartRunner
        .call(force, symbol, range.value)
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
    scope.launch(context = Dispatchers.Main) { loadOptionsChain(false) }
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

  abstract fun handleLoadTicker(
      scope: CoroutineScope,
      force: Boolean,
  )
}
