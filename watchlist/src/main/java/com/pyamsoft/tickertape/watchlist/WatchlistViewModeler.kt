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

package com.pyamsoft.tickertape.watchlist

import androidx.annotation.CheckResult
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.symbol.SymbolChangeEvent
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.ListGenerateResult
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistViewModeler
@Inject
internal constructor(
    private val state: MutableWatchlistViewState,
    private val interactor: WatchlistInteractor,
) : AbstractViewModeler<WatchlistViewState>(state) {

  private val quoteFetcher =
      highlander<ResultWrapper<List<Ticker>>, Boolean> { interactor.getQuotes(it) }

  private val searchRunner =
      highlander<ListGenerateResult<Ticker>, WatchlistViewState, List<Ticker>> { state, tickers ->
        val all = tickers.sortedWith(Ticker.COMPARATOR)
        val visible = state.asVisible(all)
        return@highlander ListGenerateResult.create(
            all = all,
            visible = visible,
        )
      }

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      interactor.listenForChanges { handleRealtimeEvent(it) }
    }
  }

  private fun CoroutineScope.handleRealtimeEvent(event: SymbolChangeEvent) =
      when (event) {
        is SymbolChangeEvent.Delete -> handleDeleteSymbol(event.symbol.symbol(), event.offerUndo)
        is SymbolChangeEvent.Insert -> handleInsertSymbol(event.symbol.symbol())
        is SymbolChangeEvent.Update -> handleUpdateSymbol(event.symbol.symbol())
      }

  private fun CoroutineScope.handleInsertSymbol(symbol: StockSymbol) {
    // Don't actually insert anything to the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    Timber.d("Refresh list on symbol insert: $symbol")
    handleRefreshList(scope = this, force = true)
  }

  private fun CoroutineScope.handleUpdateSymbol(symbol: StockSymbol) {
    // Don't actually update anything in the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    Timber.d("Refresh list on symbol update: $symbol")
    handleRefreshList(scope = this, force = true)
  }

  private fun CoroutineScope.handleDeleteSymbol(symbol: StockSymbol, offerUndo: Boolean) {
    val s = state
    s.regenerateTickers(this) { s.allTickers.filterNot { it.symbol == symbol } }

    // TODO offer up undo ability
    // On delete, we don't need to re-fetch quotes from the network
  }

  private inline fun MutableWatchlistViewState.regenerateTickers(
      scope: CoroutineScope,
      crossinline tickers: () -> List<Ticker>
  ) {
    val self = this

    // Default dispatcher for work
    scope.launch(context = Dispatchers.Default) {
      // Cancel any old processing
      try {
        val result = searchRunner.call(self, tickers())
        self.allTickers = result.all
        self.watchlist = result.visible
      } catch (e: Throwable) {
        e.ifNotCancellation {
          Timber.e(e, "Error occurred while regenerating list")

          // Clear data on bad processing
          self.allTickers = emptyList()
          self.watchlist = emptyList()
        }
      }
    }
  }

  @CheckResult
  private fun WatchlistViewState.asVisible(tickers: List<Ticker>): List<Ticker> {
    val search = this.query
    val section = this.section
    return tickers
        .asSequence()
        .filter { qs ->
          val symbol = qs.symbol.symbol()
          val name = qs.quote?.company()?.company()
          return@filter if (symbol.contains(search, ignoreCase = true)) true
          else name?.contains(search, ignoreCase = true) ?: false
        }
        .filter { qs ->
          // If the quote is null, always show this because it was a bad network fetch
          val type = qs.quote?.type() ?: return@filter true
          return@filter when (section) {
            EquityType.STOCK -> type == EquityType.STOCK
            EquityType.OPTION -> type == EquityType.OPTION
            EquityType.CRYPTOCURRENCY -> type == EquityType.CRYPTOCURRENCY
          }
        }
        .toList()
  }

  fun handleRefreshList(scope: CoroutineScope, force: Boolean) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      quoteFetcher
          .call(force)
          .onSuccess { list ->
            state.apply {
              regenerateTickers(scope) { list }
              error = null
            }
          }
          .onFailure { Timber.e(it, "Failed to refresh entry list") }
          .onFailure {
            state.apply {
              regenerateTickers(scope) { emptyList() }
              error = it
            }
          }
          .onFinally { state.isLoading = false }
    }
  }

  fun handleSearch(query: String) {
    state.query = query
  }

  fun handleSectionChanged(tab: EquityType) {
    state.section = tab
  }

  fun handleRegenerateList(scope: CoroutineScope) {
    val s = state
    s.regenerateTickers(scope) { s.allTickers }
  }

  override fun restoreState(savedInstanceState: UiSavedStateReader) {
    savedInstanceState.get<String>(KEY_SEARCH)?.also { search -> state.query = search }
  }

  override fun saveState(outState: UiSavedStateWriter) {
    state.query.also { search ->
      if (search.isBlank()) {
        outState.remove(KEY_SEARCH)
      } else {
        outState.put(KEY_SEARCH, search.trim())
      }
    }
  }

  companion object {

    private const val KEY_SEARCH = "search"
  }
}
