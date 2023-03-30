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

package com.pyamsoft.tickertape.home

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.tickertape.portfolio.PortfolioInteractor
import com.pyamsoft.tickertape.portfolio.PortfolioProcessor
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.chart.ChartDataProcessor
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockScreener
import com.pyamsoft.tickertape.stocks.api.asSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeViewModeler
@Inject
internal constructor(
    override val state: MutableHomeViewState,
    private val homeInteractor: HomeInteractor,
    private val homeInteractorCache: HomeInteractor.Cache,
    private val portfolioInteractor: PortfolioInteractor,
    private val portfolioInteractorCache: PortfolioInteractor.Cache,
    private val tickerInteractor: TickerInteractor,
    private val tickerInteractorCache: TickerInteractor.Cache,
    private val portfolioProcessor: PortfolioProcessor,
    private val chartDataProcessor: ChartDataProcessor,
) : AbstractViewModeler<HomeViewState>(state) {

  @CheckResult
  private suspend fun List<Ticker>.asHomeStocks(): List<HomeStock> {
    return this.map { t ->
      val chart = t.chart
      return@map HomeStock(
          ticker = t,
          chart = if (chart == null) null else chartDataProcessor.processChartEntries(chart),
      )
    }
  }

  @CheckResult
  private suspend inline fun handleFetchScreener(
      force: Boolean,
      screener: StockScreener,
      onLoading: (HomeBaseViewState.LoadingState) -> Unit,
      onFetched: (List<Ticker>, Throwable?) -> Unit,
  ) {
    if (force) {
      homeInteractorCache.invalidateScreener(screener)
    }

    onLoading(HomeBaseViewState.LoadingState.LOADING)
    homeInteractor
        .getScreener(screener, WATCHLIST_COUNT)
        .onSuccess { onFetched(it, null) }
        .onFailure { Timber.e(it, "Failed to fetch screener: $screener") }
        .onFailure { onFetched(emptyList(), it) }
        .onFinally { onLoading(HomeBaseViewState.LoadingState.DONE) }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_SETTINGS) { s.isSettingsOpen.value }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry
        .consumeRestored(KEY_SETTINGS)
        ?.let { it as Boolean }
        ?.also { s.isSettingsOpen.value = it }
  }

  fun handleFetchPortfolio(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      s.isLoadingPortfolio.value = HomeBaseViewState.LoadingState.LOADING

      if (force) {
        portfolioInteractorCache.invalidatePortfolio()
      }

      portfolioInteractor
          .getPortfolio()
          .onSuccess { stocks ->
            s.apply {
              portfolio.value = portfolioProcessor.process(stocks)
              portfolioError.value = null
            }
          }
          .onFailure { Timber.e(it, "Failed to fetch portfolio") }
          .onFailure {
            s.apply {
              portfolio.value = null
              portfolioError.value = it
            }
          }
          .onFinally { s.isLoadingPortfolio.value = HomeBaseViewState.LoadingState.DONE }
    }
  }

  fun handleFetchIndexes(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      s.isLoadingIndexes.value = HomeBaseViewState.LoadingState.LOADING

      if (force) {
        tickerInteractorCache.invalidateCharts(
            symbols = INDEXES,
            range = StockChart.IntervalRange.ONE_DAY,
        )
      }

      tickerInteractor
          .getCharts(
              symbols = INDEXES,
              range = StockChart.IntervalRange.ONE_DAY,
              options = null,
          )
          .onSuccess { list ->
            s.apply {
              indexes.value = list.asHomeStocks()
              indexesError.value = null
            }
          }
          .onFailure { Timber.e(it, "Failed to fetch indexes") }
          .onFailure {
            s.apply {
              indexes.value = emptyList()
              indexesError.value = null
            }
          }
          .onFinally { s.isLoadingIndexes.value = HomeBaseViewState.LoadingState.DONE }
    }
  }

  fun handleFetchGainers(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      handleFetchScreener(
          force,
          StockScreener.DAY_GAINERS,
          onLoading = { s.isLoadingGainers.value = it },
          onFetched = { list, error ->
            s.apply {
              gainers.value = list.asHomeStocks()
              gainersError.value = error
            }
          },
      )
    }
  }

  fun handleFetchGrowthTech(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      handleFetchScreener(
          force,
          StockScreener.GROWTH_TECHNOLOGY_STOCKS,
          onLoading = { s.isLoadingGrowthTech.value = it },
          onFetched = { list, error ->
            s.apply {
              growthTech.value = list.asHomeStocks()
              growthTechError.value = error
            }
          },
      )
    }
  }

  fun handleFetchUndervaluedGrowth(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      handleFetchScreener(
          force,
          StockScreener.UNDERVALUED_GROWTH_STOCKS,
          onLoading = { s.isLoadingUndervaluedGrowth.value = it },
          onFetched = { list, error ->
            s.apply {
              undervaluedGrowth.value = list.asHomeStocks()
              undervaluedGrowthError.value = error
            }
          },
      )
    }
  }

  fun handleFetchMostActive(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      handleFetchScreener(
          force,
          StockScreener.MOST_ACTIVES,
          onLoading = { s.isLoadingMostActive.value = it },
          onFetched = { list, error ->
            s.apply {
              mostActive.value = list.asHomeStocks()
              mostActiveError.value = error
            }
          },
      )
    }
  }

  fun handleFetchLosers(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      handleFetchScreener(
          force,
          StockScreener.DAY_LOSERS,
          onLoading = { s.isLoadingLosers.value = it },
          onFetched = { list, error ->
            s.apply {
              losers.value = list.asHomeStocks()
              losersError.value = error
            }
          },
      )
    }
  }

  fun handleFetchMostShorted(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      handleFetchScreener(
          force,
          StockScreener.MOST_SHORTED_STOCKS,
          onLoading = { s.isLoadingMostShorted.value = it },
          onFetched = { list, error ->
            s.apply {
              mostShorted.value = list.asHomeStocks()
              mostShortedError.value = error
            }
          },
      )
    }
  }

  fun handleFetchTrending(scope: CoroutineScope, force: Boolean) {
    val s = state
    s.isLoadingTrending.value = HomeBaseViewState.LoadingState.LOADING
    scope.launch(context = Dispatchers.Main) {
      if (force) {
        homeInteractorCache.invalidateTrending()
      }

      homeInteractor
          .getTrending(TRENDING_COUNT)
          .onSuccess { list ->
            s.apply {
              trending.value = list.asHomeStocks()
              trendingError.value = null
            }
          }
          .onFailure { Timber.e(it, "Failed to fetch trending") }
          .onFailure {
            s.apply {
              trending.value = emptyList()
              trendingError.value = it
            }
          }
          .onFinally { s.isLoadingTrending.value = HomeBaseViewState.LoadingState.DONE }
    }
  }

  fun handleOpenSettings() {
    state.isSettingsOpen.value = true
  }

  fun handleCloseSettings() {
    state.isSettingsOpen.value = false
  }

  companion object {

    private const val KEY_SETTINGS = "is_settings_open"

    private const val TRENDING_COUNT = 20
    private const val WATCHLIST_COUNT = 10
    private val INDEXES =
        listOf(
                "^GSPC",
                "^DJI",
                "^IXIC",
                "^RUT",
                "^VIX",
                "^TNX",
                "^FTSE",
                "^N225",
                "^CMC200",
                "ES=F",
                "NQ=F",
                "YM=F",
                "RTY=F",
                "GC=F",
                "SI=F",
                "CL=F",
            )
            .map { it.asSymbol() }
  }
}
