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
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.symbol.SymbolChangeEvent
import com.pyamsoft.tickertape.main.MainSelectionEvent
import com.pyamsoft.tickertape.main.TopLevelMainPage
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.ListGenerateResult
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigParams
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistViewModeler
@Inject
internal constructor(
    override val state: MutableWatchlistViewState,
    private val interactor: WatchlistInteractor,
    private val interactorCache: WatchlistInteractor.Cache,
    private val mainSelectionConsumer: EventConsumer<MainSelectionEvent>,
) : AbstractViewModeler<WatchlistViewState>(state) {

  private var internalAllTickers: List<Ticker> = emptyList()

  private val quoteFetcher =
      highlander<ResultWrapper<List<Ticker>>, Boolean> { force ->
        if (force) {
          interactorCache.invalidateQuotes()
        }

        return@highlander interactor.getQuotes()
      }

  private val watchlistGenerator =
      highlander<ListGenerateResult<Ticker>, WatchlistViewState, List<Ticker>> { state, tickers ->
        val all = tickers.sortedWith(Ticker.COMPARATOR)
        val visible = state.asVisible(all)
        return@highlander ListGenerateResult.create(
            all = all,
            visible = visible,
        )
      }

  private fun CoroutineScope.handleRealtimeEvent(event: SymbolChangeEvent) =
      when (event) {
        is SymbolChangeEvent.Delete -> handleDeleteSymbol(event.symbol.symbol, event.offerUndo)
        is SymbolChangeEvent.Insert -> handleInsertSymbol(event.symbol.symbol)
        is SymbolChangeEvent.Update -> handleUpdateSymbol(event.symbol.symbol)
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
    s.regenerateTickers(this) { internalAllTickers.filterNot { it.symbol == symbol } }

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
        val result = watchlistGenerator.call(self, tickers())
        internalAllTickers = result.all
        self.watchlist.value = result.visible
      } catch (e: Throwable) {
        e.ifNotCancellation {
          Timber.e(e, "Error occurred while regenerating list")

          // Clear data on bad processing
          internalAllTickers = emptyList()
          self.watchlist.value = emptyList()
        }
      }
    }
  }

  @CheckResult
  private fun WatchlistViewState.asVisible(tickers: List<Ticker>): List<Ticker> {
    val search = this.query.value
    val section = this.section.value
    return tickers
        .asSequence()
        .filter { qs ->
          val symbol = qs.symbol.raw
          val name = qs.quote?.company?.company
          return@filter if (symbol.contains(search, ignoreCase = true)) true
          else name?.contains(search, ignoreCase = true) ?: false
        }
        .filter { qs ->
          // If the quote is null, always show this because it was a bad network fetch
          val type = qs.quote?.type ?: return@filter true
          return@filter when (section) {
            EquityType.STOCK -> type == EquityType.STOCK
            EquityType.OPTION -> type == EquityType.OPTION
            EquityType.CRYPTOCURRENCY -> type == EquityType.CRYPTOCURRENCY
          }
        }
        .toList()
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_SEARCH) { s.query.value }.also { add(it) }
        registry.registerProvider(KEY_DELETE) { s.deleteTicker.value?.symbol?.raw }.also { add(it) }
        registry.registerProvider(KEY_DIG) { s.digParams.value }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry.consumeRestored(KEY_SEARCH)?.let { it as String }?.also { s.query.value = it }
    registry
        .consumeRestored(KEY_DELETE)
        ?.let { it as String }
        ?.asSymbol()
        ?.let { Ticker(it) }
        .also { s.deleteTicker.value = it }

    registry
        .consumeRestored(KEY_DIG)
        ?.let { it as WatchlistDigParams }
        ?.also { s.digParams.value = it }
  }

  fun handleRefreshList(scope: CoroutineScope, force: Boolean) {
    if (state.loadingState.value == WatchlistViewState.LoadingState.LOADING) {
      return
    }

    state.loadingState.value = WatchlistViewState.LoadingState.LOADING
    scope.launch(context = Dispatchers.Main) {
      quoteFetcher
          .call(force)
          .onSuccess { list ->
            state.apply {
              regenerateTickers(scope) { list }
              error.value = null
            }
          }
          .onFailure { Timber.e(it, "Failed to refresh entry list") }
          .onFailure {
            state.apply {
              regenerateTickers(scope) { emptyList() }
              error.value = it
            }
          }
          .onFinally { state.loadingState.value = WatchlistViewState.LoadingState.DONE }
    }
  }

  fun bind(
      scope: CoroutineScope,
      onMainSelectionEvent: () -> Unit,
  ) {
    scope.launch(context = Dispatchers.Main) {
      interactor.listenForChanges { handleRealtimeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) {
      mainSelectionConsumer.onEvent { event ->
        if (event.page == TopLevelMainPage.Watchlist) {
          onMainSelectionEvent()
        }
      }
    }
  }

  fun handleSearch(query: String) {
    state.query.value = query
  }

  fun handleSectionChanged(tab: EquityType) {
    state.section.value = tab
  }

  fun handleRegenerateList(scope: CoroutineScope) {
    val s = state
    s.regenerateTickers(scope) { internalAllTickers }
  }

  fun handleOpenDeleteTicker(ticker: Ticker) {
    state.deleteTicker.value = ticker
  }

  fun handleCloseDeleteTicker() {
    state.deleteTicker.value = null
  }

  fun handleOpenDig(ticker: Ticker) {
    val quote = ticker.quote
    if (quote == null) {
      Timber.w("Can't show dig dialog, missing quote: $ticker")
      return
    }

    state.digParams.value =
        WatchlistDigParams(
            symbol = quote.symbol,
            lookupSymbol = if (quote is StockOptionsQuote) quote.underlyingSymbol else quote.symbol,
            equityType = quote.type,
        )
  }

  fun handleCloseDig() {
    state.digParams.value = null
  }

  companion object {

    private const val KEY_SEARCH = "search"
    private const val KEY_DIG = "dig"
    private const val KEY_DELETE = "delete"
  }
}
