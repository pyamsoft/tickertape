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

package com.pyamsoft.tickertape.home

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.tickertape.portfolio.PortfolioInteractor
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockScreener
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.watchlist.WatchlistInteractor
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModeler
@Inject
internal constructor(
    override val state: MutableHomeViewState,
    private val homeInteractor: HomeInteractor,
    private val homeInteractorCache: HomeInteractor.Cache,
    private val portfolioInteractor: PortfolioInteractor,
    private val portfolioInteractorCache: PortfolioInteractor.Cache,
    private val watchlistInteractor: WatchlistInteractor,
    private val watchlistInteractorCache: WatchlistInteractor.Cache,
    private val tickerInteractor: TickerInteractor,
    private val tickerInteractorCache: TickerInteractor.Cache,
) : AbstractViewModeler<HomeViewState>(state) {

  private var internalFullWatchlist: List<Ticker> = emptyList()

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

  private fun handleGenerateWatchlist(list: List<Ticker>) {
    val s = state
    val sorted = list.sortedWith(Ticker.COMPARATOR)
    s.apply {
      internalFullWatchlist = sorted
      watchlist.value = sorted.take(WATCHLIST_COUNT)
    }
  }

  fun handleFetchWatchlist(scope: CoroutineScope, force: Boolean) {
    val s = state

    s.isLoadingWatchlist.value = HomeBaseViewState.LoadingState.LOADING
    scope.launch(context = Dispatchers.Main) {
      if (force) {
        watchlistInteractorCache.invalidateQuotes()
      }
      watchlistInteractor
          .getQuotes()
          .onSuccess { list ->
            handleGenerateWatchlist(list)
            s.watchlistError.value = null
          }
          .onFailure { Timber.e(it, "Failed to fetch watchlist") }
          .onFailure {
            s.apply {
              internalFullWatchlist = emptyList()
              watchlist.value = emptyList()
              watchlistError.value = it
            }
          }
          .onFinally { s.isLoadingWatchlist.value = HomeBaseViewState.LoadingState.DONE }
    }
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
          .onSuccess {
            s.apply {
              portfolio.value = PortfolioStockList.of(it)
              portfolioError.value = null
            }
          }
          .onFailure { Timber.e(it, "Failed to fetch portfolio") }
          .onFailure {
            s.apply {
              portfolio.value = PortfolioStockList.empty()
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
          .onSuccess {
            s.apply {
              indexes.value = it
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
              gainers.value = list
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
              growthTech.value = list
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
              undervaluedGrowth.value = list
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
              mostActive.value = list
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
              losers.value = list
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
              mostShorted.value = list
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
          .onSuccess {
            s.apply {
              trending.value = it
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

  companion object {

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
