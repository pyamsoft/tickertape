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
    private val state: MutableHomeViewState,
    private val homeInteractor: HomeInteractor,
    private val homeInteractorCache: HomeInteractor.Cache,
    private val portfolioInteractor: PortfolioInteractor,
    private val portfolioInteractorCache: PortfolioInteractor.Cache,
    private val watchlistInteractor: WatchlistInteractor,
    private val watchlistInteractorCache: WatchlistInteractor.Cache,
    private val tickerInteractor: TickerInteractor,
    private val tickerInteractorCache: TickerInteractor.Cache,
) : AbstractViewModeler<HomeViewState>(state) {

  @CheckResult
  private suspend inline fun handleFetchScreener(
      force: Boolean,
      screener: StockScreener,
      onLoading: (Boolean) -> Unit,
      onFetched: (List<Ticker>, Throwable?) -> Unit,
  ) {
    if (force) {
      homeInteractorCache.invalidateScreener(screener)
    }

    onLoading(true)
    homeInteractor
        .getScreener(screener, WATCHLIST_COUNT)
        .onSuccess { onFetched(it, null) }
        .onFailure { Timber.e(it, "Failed to fetch screener: $screener") }
        .onFailure { onFetched(emptyList(), it) }
        .onFinally { onLoading(false) }
  }

  private fun handleGenerateWatchlist(list: List<Ticker>) {
    val s = state
    val sorted = list.sortedWith(Ticker.COMPARATOR)
    s.apply {
      fullWatchlist = sorted
      watchlist = sorted.take(WATCHLIST_COUNT)
    }
  }

  fun handleFetchWatchlist(scope: CoroutineScope, force: Boolean) {
    val s = state
    s.isLoadingWatchlist = true
    scope.launch(context = Dispatchers.Main) {
      if (force) {
        watchlistInteractorCache.invalidateQuotes()
      }
      watchlistInteractor
          .getQuotes()
          .onSuccess { list ->
            handleGenerateWatchlist(list)
            s.watchlistError = null
          }
          .onFailure { Timber.e(it, "Failed to fetch watchlist") }
          .onFailure {
            s.apply {
              fullWatchlist = emptyList()
              watchlist = emptyList()
              watchlistError = it
            }
          }
          .onFinally { s.isLoadingWatchlist = false }
    }
  }

  fun handleFetchPortfolio(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      s.isLoadingPortfolio = true

      if (force) {
        portfolioInteractorCache.invalidatePortfolio()
      }

      portfolioInteractor
          .getPortfolio()
          .onSuccess {
            s.apply {
              portfolio = PortfolioStockList.of(it)
              portfolioError = null
            }
          }
          .onFailure { Timber.e(it, "Failed to fetch portfolio") }
          .onFailure {
            s.apply {
              portfolio = PortfolioStockList.empty()
              portfolioError = it
            }
          }
          .onFinally { s.isLoadingPortfolio = false }
    }
  }

  fun handleFetchIndexes(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      s.isLoadingIndexes = true

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
              indexes = it
              indexesError = null
            }
          }
          .onFailure { Timber.e(it, "Failed to fetch indexes") }
          .onFailure {
            s.apply {
              indexes = emptyList()
              indexesError = null
            }
          }
          .onFinally { s.isLoadingIndexes = false }
    }
  }

  fun handleFetchGainers(scope: CoroutineScope, force: Boolean) {
    val s = state
    scope.launch(context = Dispatchers.Main) {
      handleFetchScreener(
          force,
          StockScreener.DAY_GAINERS,
          onLoading = { s.isLoadingGainers = it },
          onFetched = { list, error ->
            s.apply {
              gainers = list
              gainersError = error
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
          onLoading = { s.isLoadingGrowthTech = it },
          onFetched = { list, error ->
            s.apply {
              growthTech = list
              growthTechError = error
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
          onLoading = { s.isLoadingUndervaluedGrowth = it },
          onFetched = { list, error ->
            s.apply {
              undervaluedGrowth = list
              undervaluedGrowthError = error
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
          onLoading = { s.isLoadingMostActive = it },
          onFetched = { list, error ->
            s.apply {
              mostActive = list
              mostActiveError = error
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
          onLoading = { s.isLoadingLosers = it },
          onFetched = { list, error ->
            s.apply {
              losers = list
              losersError = error
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
          onLoading = { s.isLoadingMostShorted = it },
          onFetched = { list, error ->
            s.apply {
              mostShorted = list
              mostShortedError = error
            }
          },
      )
    }
  }

  fun handleFetchTrending(scope: CoroutineScope, force: Boolean) {
    val s = state
    s.isLoadingTrending = true
    scope.launch(context = Dispatchers.Main) {
      if (force) {
        homeInteractorCache.invalidateTrending()
      }

      homeInteractor
          .getTrending(TRENDING_COUNT)
          .onSuccess {
            s.apply {
              trending = it
              trendingError = null
            }
          }
          .onFailure { Timber.e(it, "Failed to fetch trending") }
          .onFailure {
            s.apply {
              trending = emptyList()
              trendingError = it
            }
          }
          .onFinally { s.isLoadingTrending = false }
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
