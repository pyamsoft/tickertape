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

import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.tickertape.portfolio.PortfolioInteractor
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.watchlist.WatchlistInteractor
import javax.inject.Inject
import kotlinx.coroutines.*
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
        // Make sure we only show value of stocks in the home portfolio
        .map { tickers -> tickers.filter { it.holding.type() == EquityType.STOCK } }
        .onSuccess {
          state.apply {
            portfolio = it
            portfolioError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch portfolio") }
        .onFailure {
          state.apply {
            portfolio = emptyList()
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

  private suspend fun fetchGainers(force: Boolean) {
    state.isLoadingGainers = true
    homeInteractor
        .getDayGainers(force, WATCHLIST_COUNT)
        .onSuccess {
          state.apply {
            gainers = it
            gainersError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch gainers") }
        .onFailure {
          state.apply {
            gainers = emptyList()
            gainersError = it
          }
        }
        .onFinally { state.isLoadingGainers = false }
  }

  private suspend fun fetchLosers(force: Boolean) {
    state.isLoadingLosers = true
    homeInteractor
        .getDayLosers(force, WATCHLIST_COUNT)
        .onSuccess {
          state.apply {
            losers = it
            losersError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch losers") }
        .onFailure {
          state.apply {
            losers = emptyList()
            losersError = it
          }
        }
        .onFinally { state.isLoadingLosers = false }
  }

  private suspend fun fetchMostShorted(force: Boolean) {
    state.isLoadingMostShorted = true
    homeInteractor
        .getDayShorted(force, WATCHLIST_COUNT)
        .onSuccess {
          state.apply {
            mostShorted = it
            mostShortedError = null
          }
        }
        .onFailure { Timber.e(it, "Failed to fetch most shorted") }
        .onFailure {
          state.apply {
            mostShorted = emptyList()
            mostShortedError = null
          }
        }
        .onFinally { state.isLoadingMostShorted = false }
  }

  private suspend fun fetchTrending(force: Boolean) {
    state.isLoadingTrending = true
    homeInteractor
        .getDayTrending(force, TRENDING_COUNT)
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
    private val INDEXES = listOf("^GSPC", "^DJI", "^IXIC", "^RUT", "^VIX").map { it.asSymbol() }
  }
}
