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
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.BottomOffset
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError
import com.pyamsoft.tickertape.watchlist.WatchlistInteractor
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class HomeViewModel
@Inject
internal constructor(
    private val interactor: HomeInteractor,
    private val portfolioInteractor: PortfolioInteractor,
    private val watchlistInteractor: WatchlistInteractor,
    private val quoteInteractor: QuoteInteractor,
    private val bottomOffsetBus: EventConsumer<BottomOffset>,
    private val mainPageBus: EventBus<MainPage>,
) :
    UiViewModel<HomeViewState, HomeControllerEvent>(
        initialState =
            HomeViewState(
                portfolio = PortfolioStockList(emptyList()).pack(),
                isLoadingPortfolio = false,
                watchlist = emptyList<QuotedStock>().pack(),
                isLoadingWatchlist = false,
                indexes = emptyList<QuotedChart>().pack(),
                isLoadingIndexes = false,
                gainers = emptyList<TopDataWithChart>().pack(),
                isLoadingGainers = false,
                losers = emptyList<TopDataWithChart>().pack(),
                isLoadingLosers = false,
                trending = emptyList<TopDataWithChart>().pack(),
                isLoadingTrending = false,
                mostShorted = emptyList<TopDataWithChart>().pack(),
                isLoadingMostShorted = false,
                bottomOffset = 0,
            )) {

  private val portfolioFetcher =
      highlander<ResultWrapper<List<PortfolioStock>>, Boolean> {
        portfolioInteractor.getPortfolio(it)
      }

  private val watchlistFetcher =
      highlander<ResultWrapper<List<QuotedStock>>, Boolean> {
        watchlistInteractor
            .getQuotes(it)
            .map { quotes -> quotes.sortedWith(QuotedStock.COMPARATOR) }
            .map { quotes -> quotes.take(WATCHLIST_COUNT) }
      }

  private val indexesFetcher =
      highlander<ResultWrapper<List<QuotedChart>>, Boolean> { force ->
        quoteInteractor.getCharts(
            force = force,
            symbols = INDEXES,
            range = StockChart.IntervalRange.ONE_DAY,
            includeQuote = true)
      }

  private val gainersFetcher =
      highlander<ResultWrapper<List<TopDataWithChart>>, Boolean> {
        interactor.getDayGainers(it, WATCHLIST_COUNT)
      }

  private val losersFetcher =
      highlander<ResultWrapper<List<TopDataWithChart>>, Boolean> {
        interactor.getDayLosers(it, WATCHLIST_COUNT)
      }

  private val shortedFetcher =
      highlander<ResultWrapper<List<TopDataWithChart>>, Boolean> {
        interactor.getMostShorted(it, WATCHLIST_COUNT)
      }

  private val trendingFetcher =
      highlander<ResultWrapper<List<TopDataWithChart>>, Boolean> {
        interactor.getDayTrending(it, TRENDING_COUNT)
      }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      bottomOffsetBus.onEvent { setState { copy(bottomOffset = it.height) } }
    }
  }

  private suspend fun fetchWatchlist(force: Boolean) =
      withContext(context = Dispatchers.Default) {
        setState(
            stateChange = { copy(isLoadingWatchlist = true) },
            andThen = {
              watchlistFetcher
                  .call(force)
                  .onSuccess {
                    setState { copy(watchlist = it.pack(), isLoadingWatchlist = false) }
                  }
                  .onFailure { Timber.e(it, "Failed to fetch watchlist") }
                  .onFailure {
                    setState { copy(watchlist = it.packError(), isLoadingWatchlist = false) }
                  }
            })
      }

  private suspend fun fetchPortfolio(force: Boolean) =
      withContext(context = Dispatchers.Default) {
        setState(
            stateChange = { copy(isLoadingPortfolio = true) },
            andThen = {
              portfolioFetcher
                  .call(force)
                  .onSuccess {
                    setState {
                      copy(portfolio = PortfolioStockList(it).pack(), isLoadingPortfolio = false)
                    }
                  }
                  .onFailure { Timber.e(it, "Failed to fetch portfolio") }
                  .onFailure {
                    setState { copy(portfolio = it.packError(), isLoadingPortfolio = false) }
                  }
            })
      }

  private fun CoroutineScope.fetchIndexes(force: Boolean) {
    setState(
        stateChange = { copy(isLoadingIndexes = true) },
        andThen = {
          indexesFetcher
              .call(force)
              .onSuccess { setState { copy(indexes = it.pack(), isLoadingIndexes = false) } }
              .onFailure { Timber.e(it, "Failed to fetch indexes") }
              .onFailure { setState { copy(indexes = it.packError(), isLoadingIndexes = false) } }
        })
  }

  private fun CoroutineScope.fetchGainers(force: Boolean) {
    setState(
        stateChange = { copy(isLoadingGainers = true) },
        andThen = {
          gainersFetcher
              .call(force)
              .onSuccess { setState { copy(gainers = it.pack(), isLoadingGainers = false) } }
              .onFailure { Timber.e(it, "Failed to fetch gainers") }
              .onFailure { setState { copy(gainers = it.packError(), isLoadingGainers = false) } }
        })
  }

  private fun CoroutineScope.fetchLosers(force: Boolean) {
    setState(
        stateChange = { copy(isLoadingLosers = true) },
        andThen = {
          losersFetcher
              .call(force)
              .onSuccess { setState { copy(losers = it.pack(), isLoadingLosers = false) } }
              .onFailure { Timber.e(it, "Failed to fetch losers") }
              .onFailure { setState { copy(losers = it.packError(), isLoadingLosers = false) } }
        })
  }

  private fun CoroutineScope.fetchMostShorted(force: Boolean) {
    setState(
        stateChange = { copy(isLoadingMostShorted = true) },
        andThen = {
          shortedFetcher
              .call(force)
              .onSuccess {
                setState { copy(mostShorted = it.pack(), isLoadingMostShorted = false) }
              }
              .onFailure { Timber.e(it, "Failed to fetch most shorted") }
              .onFailure {
                setState { copy(mostShorted = it.packError(), isLoadingMostShorted = false) }
              }
        })
  }

  private fun CoroutineScope.fetchTrending(force: Boolean) {
    setState(
        stateChange = { copy(isLoadingTrending = true) },
        andThen = {
          trendingFetcher
              .call(force)
              .onSuccess { setState { copy(trending = it.pack(), isLoadingTrending = false) } }
              .onFailure { Timber.e(it, "Failed to fetch trending") }
              .onFailure { setState { copy(trending = it.packError(), isLoadingTrending = false) } }
        })
  }

  fun handleFetchPortfolio(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchPortfolio(force) }
  }

  fun handleFetchWatchlist(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchWatchlist(force) }
  }

  fun handleFetchIndexes(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchIndexes(force) }
  }

  fun handleFetchGainers(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchGainers(force) }
  }

  fun handleFetchLosers(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchLosers(force) }
  }

  fun handleFetchTrending(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchTrending(force) }
  }

  fun handleFetchMostShorted(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchMostShorted(force) }
  }

  fun handleOpenPage(page: MainPage) {
    viewModelScope.launch(context = Dispatchers.Default) { mainPageBus.send(page) }
  }

  fun handleDigWatchlistSymbol(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val data = state.watchlist
      if (data !is PackedData.Data<List<QuotedStock>>) {
        Timber.w("Cannot dig symbol in error state: $data")
        return@launch
      }

      val quote = data.value[index]
      publish(HomeControllerEvent.ManageWatchlistSymbol(quote))
    }
  }

  companion object {

    private const val TRENDING_COUNT = 20
    private const val WATCHLIST_COUNT = 10
    private val INDEXES = listOf("^GSPC", "^DJI", "^IXIC", "^RUT").map { it.asSymbol() }
  }
}
