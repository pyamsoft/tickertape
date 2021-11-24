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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.portfolio.PortfolioInteractor
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.BottomOffset
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.TopOffset
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError
import com.pyamsoft.tickertape.watchlist.WatchlistInteractor
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel
@Inject
internal constructor(
    private val homeInteractor: HomeInteractor,
    private val portfolioInteractor: PortfolioInteractor,
    private val watchlistInteractor: WatchlistInteractor,
    private val tickerInteractor: TickerInteractor,
    private val mainPageBus: EventBus<MainPage>,
    topOffsetBus: EventConsumer<TopOffset>,
    bottomOffsetBus: EventConsumer<BottomOffset>,
) :
    UiViewModel<HomeViewState, HomeControllerEvent>(
        initialState =
            HomeViewState(
                isLoading = false,
                portfolio = emptyList<PortfolioStock>().pack(),
                watchlist = emptyList<Ticker>().pack(),
                indexes = emptyList<Ticker>().pack(),
                gainers = emptyList<TopDataWithChart>().pack(),
                losers = emptyList<TopDataWithChart>().pack(),
                trending = emptyList<TopDataWithChart>().pack(),
                mostShorted = emptyList<TopDataWithChart>().pack(),
                topOffset = 0,
                bottomOffset = 0,
            ),
    ) {

  private val homeFetcher =
      highlander<ResultWrapper<Unit>, Boolean> { force ->
        awaitAll(
            async { fetchWatchlist(force) },
            async { fetchPortfolio(force) },
            async { fetchTrending(force) },
            async { fetchLosers(force) },
            async { fetchIndexes(force) },
            async { fetchGainers(force) },
            async { fetchMostShorted(force) },
        )
        return@highlander ResultWrapper.success(Unit)
      }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      bottomOffsetBus.onEvent { setState { copy(bottomOffset = it.height) } }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      topOffsetBus.onEvent { setState { copy(topOffset = it.height) } }
    }
  }

  private suspend fun fetchWatchlist(force: Boolean) {
    watchlistInteractor
        .getQuotes(force)
        .map { quotes -> quotes.sortedWith(Ticker.COMPARATOR) }
        .map { quotes -> quotes.take(WATCHLIST_COUNT) }
        .onSuccess { setState { copy(watchlist = it.pack()) } }
        .onFailure { Timber.e(it, "Failed to fetch watchlist") }
        .onFailure { setState { copy(watchlist = it.packError()) } }
  }

  private suspend fun fetchPortfolio(force: Boolean) {
    portfolioInteractor
        .getPortfolio(force)
        // Make sure we only show value of stocks in the home portfolio
        .map { result -> result.filter { it.holding.type() == EquityType.STOCK } }
        .onSuccess { setState { copy(portfolio = it.pack()) } }
        .onFailure { Timber.e(it, "Failed to fetch portfolio") }
        .onFailure { setState { copy(portfolio = it.packError()) } }
  }

  private suspend fun fetchIndexes(force: Boolean) {
    tickerInteractor
        .getCharts(
            force = force,
            symbols = INDEXES,
            range = StockChart.IntervalRange.ONE_DAY,
        )
        .onSuccess { setState { copy(indexes = it.pack()) } }
        .onFailure { Timber.e(it, "Failed to fetch indexes") }
        .onFailure { setState { copy(indexes = it.packError()) } }
  }

  private suspend fun fetchGainers(force: Boolean) {
    homeInteractor
        .getDayGainers(force, WATCHLIST_COUNT)
        .onSuccess { setState { copy(gainers = it.pack()) } }
        .onFailure { Timber.e(it, "Failed to fetch gainers") }
        .onFailure { setState { copy(gainers = it.packError()) } }
  }

  private suspend fun fetchLosers(force: Boolean) {
    homeInteractor
        .getDayLosers(force, WATCHLIST_COUNT)
        .onSuccess { setState { copy(losers = it.pack()) } }
        .onFailure { Timber.e(it, "Failed to fetch losers") }
        .onFailure { setState { copy(losers = it.packError()) } }
  }

  private suspend fun fetchMostShorted(force: Boolean) {
    homeInteractor
        .getMostShorted(force, WATCHLIST_COUNT)
        .onSuccess { setState { copy(mostShorted = it.pack()) } }
        .onFailure { Timber.e(it, "Failed to fetch most shorted") }
        .onFailure { setState { copy(mostShorted = it.packError()) } }
  }

  private suspend fun fetchTrending(force: Boolean) {
    homeInteractor
        .getDayTrending(force, TRENDING_COUNT)
        .onSuccess { setState { copy(trending = it.pack()) } }
        .onFailure { Timber.e(it, "Failed to fetch trending") }
        .onFailure { setState { copy(trending = it.packError()) } }
  }

  fun handleLoad(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) {
      setState(
          stateChange = { copy(isLoading = true) },
          andThen = {
            homeFetcher
                .call(force)
                .onSuccess { setState { copy(isLoading = false) } }
                .onFailure { Timber.e(it, "Error refreshing home page") }
                .onFailure { setState { copy(isLoading = false) } }
          })
    }
  }

  fun handleOpenPage(page: MainPage) {
    viewModelScope.launch(context = Dispatchers.Default) { mainPageBus.send(page) }
  }

  fun handleDigWatchlistSymbol(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val data = state.watchlist
      if (data !is PackedData.Data<List<Ticker>>) {
        Timber.w("Cannot dig symbol in error state: $data")
        return@launch
      }

      val quote = data.value[index]
      publish(HomeControllerEvent.DigWatchlistSymbol(quote))
    }
  }

  fun handleDigChart(index: Int, type: HomeChartType) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val data =
          when (type) {
            HomeChartType.INDEX -> {
              val chart = state.indexes
              if (chart is PackedData.Data<List<Ticker>>) chart.value[index].quote
              else {
                Timber.w("Cannot dig symbol in error state: $chart")
                return@launch
              }
            }
            HomeChartType.GAINER -> {
              val chart = state.gainers
              if (chart is PackedData.Data<List<TopDataWithChart>>) chart.value[index].quote
              else {
                Timber.w("Cannot dig symbol in error state: $chart")
                return@launch
              }
            }
            HomeChartType.LOSER -> {
              val chart = state.losers
              if (chart is PackedData.Data<List<TopDataWithChart>>) chart.value[index].quote
              else {
                Timber.w("Cannot dig symbol in error state: $chart")
                return@launch
              }
            }
            HomeChartType.TRENDING -> {
              val chart = state.trending
              if (chart is PackedData.Data<List<TopDataWithChart>>) chart.value[index].quote
              else {
                Timber.w("Cannot dig symbol in error state: $chart")
                return@launch
              }
            }
            HomeChartType.MOST_SHORTED -> {
              val chart = state.mostShorted
              if (chart is PackedData.Data<List<TopDataWithChart>>) chart.value[index].quote
              else {
                Timber.w("Cannot dig symbol in error state: $chart")
                return@launch
              }
            }
          }

      if (data == null) {
        Timber.w("Cannot dig chart when quote is null")
        return@launch
      }

      publish(HomeControllerEvent.DigChartSymbol(data))
    }
  }

  companion object {

    private const val TRENDING_COUNT = 20
    private const val WATCHLIST_COUNT = 10
    private val INDEXES = listOf("^GSPC", "^DJI", "^IXIC", "^RUT", "^VIX").map { it.asSymbol() }
  }
}
