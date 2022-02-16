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
import com.pyamsoft.highlander.highlander
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModeler
@Inject
internal constructor(
    private val state: MutableHomeViewState,
    private val homeInteractor: HomeInteractor,
    private val portfolioInteractor: PortfolioInteractor,
    private val watchlistInteractor: WatchlistInteractor,
    private val tickerInteractor: TickerInteractor,
) : AbstractViewModeler<HomeViewState>(state) {

  private val homeFetcher =
      highlander<Unit, Boolean> { force ->
        awaitAll(
            async { fetchWatchlist(force) },
            async { fetchPortfolio(force) },
            async { fetchTrending(force) },
            async { fetchLosers(force) },
            async { fetchIndexes(force) },
            async { fetchGainers(force) },
            async { fetchMostShorted(force) },
            async { fetchGrowthTech(force) },
            async { fetchUndervaluedGrowth(force) },
            async { fetchMostActive(force) },
        )
      }

  private suspend fun fetchWatchlist(force: Boolean) {
    state.isLoadingWatchlist = true
    watchlistInteractor
        .getQuotes(force)
        .map { it.sortedWith(Ticker.COMPARATOR) }
        .map { it.take(WATCHLIST_COUNT) }
        .onSuccess {
          state.apply {
            watchlist = it
            watchlistError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch watchlist") }
        .onFailure {
          state.apply {
            watchlist = emptyList()
            watchlistError = it
          }
        }
        .onFinally { state.isLoadingWatchlist = false }
  }

  private suspend fun fetchPortfolio(force: Boolean) {
    state.isLoadingPortfolio = true
    portfolioInteractor
        .getPortfolio(force)
        .onSuccess {
          state.apply {
            portfolio = PortfolioStockList.of(it)
            portfolioError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch portfolio") }
        .onFailure {
          state.apply {
            portfolio = PortfolioStockList.empty()
            portfolioError = it
          }
        }
        .onFinally { state.isLoadingPortfolio = false }
  }

  private suspend fun fetchIndexes(force: Boolean) {
    state.isLoadingIndexes = true
    tickerInteractor
        .getCharts(
            force = force,
            symbols = INDEXES,
            range = StockChart.IntervalRange.ONE_DAY,
            options = null,
        )
        .onSuccess {
          state.apply {
            indexes = it
            indexesError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch indexes") }
        .onFailure {
          state.apply {
            indexes = emptyList()
            indexesError = null
          }
        }
        .onFinally { state.isLoadingIndexes = false }
  }

  @CheckResult
  private suspend fun fetchScreener(
      force: Boolean,
      screener: StockScreener,
      onLoading: (Boolean) -> Unit,
      onFetched: (List<Ticker>, Throwable?) -> Unit,
  ) {
    onLoading(true)
    homeInteractor
        .getScreener(force, screener, WATCHLIST_COUNT)
        .onSuccess { onFetched(it, null) }
        .onFailure { Timber.e(it, "Failed to fetch screener: $screener") }
        .onFailure { onFetched(emptyList(), it) }
        .onFinally { onLoading(false) }
  }

  private suspend fun fetchGainers(force: Boolean) {
    fetchScreener(
        force,
        StockScreener.DAY_GAINERS,
        onLoading = { state.isLoadingGainers = it },
        onFetched = { list, error ->
          state.apply {
            gainers = list
            gainersError = error
          }
        },
    )
  }

  private suspend fun fetchGrowthTech(force: Boolean) {
    fetchScreener(
        force,
        StockScreener.GROWTH_TECHNOLOGY_STOCKS,
        onLoading = { state.isLoadingGrowthTech = it },
        onFetched = { list, error ->
          state.apply {
            growthTech = list
            growthTechError = error
          }
        },
    )
  }

  private suspend fun fetchUndervaluedGrowth(force: Boolean) {
    fetchScreener(
        force,
        StockScreener.UNDERVALUED_GROWTH_STOCKS,
        onLoading = { state.isLoadingUndervaluedGrowth = it },
        onFetched = { list, error ->
          state.apply {
            undervaluedGrowth = list
            undervaluedGrowthError = error
          }
        },
    )
  }

  private suspend fun fetchMostActive(force: Boolean) {
    fetchScreener(
        force,
        StockScreener.MOST_ACTIVES,
        onLoading = { state.isLoadingMostActive = it },
        onFetched = { list, error ->
          state.apply {
            mostActive = list
            mostActiveError = error
          }
        },
    )
  }

  private suspend fun fetchLosers(force: Boolean) {
    fetchScreener(
        force,
        StockScreener.DAY_LOSERS,
        onLoading = { state.isLoadingLosers = it },
        onFetched = { list, error ->
          state.apply {
            losers = list
            losersError = error
          }
        },
    )
  }

  private suspend fun fetchMostShorted(force: Boolean) {
    fetchScreener(
        force,
        StockScreener.MOST_SHORTED_STOCKS,
        onLoading = { state.isLoadingMostShorted = it },
        onFetched = { list, error ->
          state.apply {
            mostShorted = list
            mostShortedError = error
          }
        },
    )
  }

  private suspend fun fetchTrending(force: Boolean) {
    state.isLoadingTrending = true
    homeInteractor
        .getTrending(force, TRENDING_COUNT)
        .onSuccess {
          state.apply {
            trending = it
            trendingError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch trending") }
        .onFailure {
          state.apply {
            trending = emptyList()
            trendingError = it
          }
        }
        .onFinally { state.isLoadingTrending = false }
  }

  fun handleRefreshList(
      scope: CoroutineScope,
      force: Boolean,
  ) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      try {
        homeFetcher.call(force)
      } finally {
        state.isLoading = false
      }
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
