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
import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.symbol.SymbolChangeEvent
import com.pyamsoft.tickertape.main.AddNew
import com.pyamsoft.tickertape.main.MainAdderViewModel
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.ui.BottomOffset
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError
import com.pyamsoft.tickertape.ui.transformData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val tapeLauncher: TapeLauncher,
    private val interactor: WatchlistInteractor,
    private val bottomOffsetBus: EventConsumer<BottomOffset>,
    addNewBus: EventConsumer<AddNew>
) :
    MainAdderViewModel<WatchListViewState, WatchListControllerEvent>(
        savedState = savedState,
        addNewBus = addNewBus,
        initialState =
            WatchListViewState(
                embedded = false,
                query = "",
                section = DEFAULT_SECTION,
                isLoading = false,
                watchlist = emptyList<QuotedStock>().pack(),
                bottomOffset = 0,
            )) {

  private val quoteFetcher =
      highlander<ResultWrapper<List<QuotedStock>>, Boolean> { interactor.getQuotes(it) }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      bottomOffsetBus.onEvent { setState { copy(bottomOffset = it.height) } }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      interactor.listenForChanges { handleRealtimeEvent(it) }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      val search = restoreSavedState(KEY_SEARCH) { "" }
      setState { copy(query = search) }
    }
  }

  override fun CoroutineScope.onAddNewEvent(type: HoldingType) {
    Timber.d("Watchlist add new symbol!")
    publish(WatchListControllerEvent.AddNewSymbol(type))
  }

  private fun CoroutineScope.handleRealtimeEvent(event: SymbolChangeEvent) =
      when (event) {
        is SymbolChangeEvent.Delete -> handleDeleteSymbol(event.symbol.symbol(), event.offerUndo)
        is SymbolChangeEvent.Insert -> handleInsertSymbol(event.symbol.symbol())
        is SymbolChangeEvent.Update -> handleUpdateSymbol(event.symbol.symbol())
      }

  private fun CoroutineScope.handleInsertSymbol(symbol: StockSymbol) {
    Timber.d("New symbol inserted: $symbol")

    // Don't actually insert anything to the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    launch(context = Dispatchers.Default) { fetchQuotes(true) }
  }

  private fun CoroutineScope.handleUpdateSymbol(symbol: StockSymbol) {
    Timber.d("Existing symbol updated: $symbol")

    // Don't actually update anything in the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    launch(context = Dispatchers.Default) { fetchQuotes(true) }
  }

  private fun CoroutineScope.handleDeleteSymbol(symbol: StockSymbol, offerUndo: Boolean) {
    Timber.d("Existing symbol deleted: $symbol")
    setState {
      copy(watchlist = watchlist.transformData { q -> q.filterNot { it.symbol == symbol } })
    }
    // TODO offer up undo ability

    // On delete, we don't need to re-fetch quotes from the network
  }

  fun handleFetchQuotes(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchQuotes(force) }
  }

  private fun CoroutineScope.fetchQuotes(force: Boolean) {
    val currentSection = state.section
    setState(
        stateChange = { copy(isLoading = true) },
        andThen = {
          quoteFetcher
              .call(force)
              .map { list ->
                list.filter { qs ->
                  // If the quote is null, always show this because it was a bad network fetch
                  val type = qs.quote?.type() ?: return@filter true
                  return@filter when (currentSection) {
                    WatchlistTabSection.STOCK -> type == EquityType.STOCK
                    WatchlistTabSection.OPTION -> type == EquityType.OPTION
                    WatchlistTabSection.CRYPTO -> type == EquityType.CRYPTOCURRENCY
                  }
                }
              }
              .onSuccess {
                setState {
                  copy(watchlist = it.sortedWith(QuotedStock.COMPARATOR).pack(), isLoading = false)
                }
              }
              .onFailure { Timber.e(it, "Failed to fetch quotes") }
              .onFailure { setState { copy(watchlist = it.packError(), isLoading = false) } }
              .onSuccess { tapeLauncher.start() }
        })
  }

  fun handleSearch(query: String) {
    setState(
        stateChange = { copy(query = query) },
        andThen = { newState ->
          putSavedState(KEY_SEARCH, newState.query)
          fetchQuotes(false)
        })
  }

  @CheckResult
  private fun getDisplayedItem(index: Int): QuotedStock? {
    val data = state.displayWatchlist
    if (data !is PackedData.Data<List<WatchListViewState.DisplayWatchlist>>) {
      Timber.w("displayWatchlist is not Data: $data")
      return null
    }

    val stock = data.value[index]
    if (stock !is WatchListViewState.DisplayWatchlist.Item) {
      Timber.w("stock is not DisplayWatchlist.Item: $stock")
      return null
    }

    return stock.stock
  }

  fun handleRemove(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val stock = getDisplayedItem(index) ?: return@launch
      interactor
          .removeQuote(stock.symbol)
          .onSuccess { Timber.d("Removed quote $stock") }
          .onFailure { Timber.e(it, "Error removing quote: $stock") }
    }
  }

  fun handleDigSymbol(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val stock = getDisplayedItem(index) ?: return@launch
      publish(WatchListControllerEvent.ManageSymbol(stock))
    }
  }

  override fun handleShowStocks() {
    setState(
        stateChange = {
          copy(section = WatchlistTabSection.STOCK, watchlist = emptyList<QuotedStock>().pack())
        },
        andThen = { fetchQuotes(false) })
  }

  override fun handleShowOptions() {
    setState(
        stateChange = {
          copy(section = WatchlistTabSection.OPTION, watchlist = emptyList<QuotedStock>().pack())
        },
        andThen = { fetchQuotes(false) })
  }

  override fun handleShowCrypto() {
    setState(
        stateChange = {
          copy(section = WatchlistTabSection.CRYPTO, watchlist = emptyList<QuotedStock>().pack())
        },
        andThen = { fetchQuotes(false) })
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<WatchlistViewModel> {
    override fun create(savedState: UiSavedState): WatchlistViewModel
  }

  companion object {

    private const val KEY_SEARCH = "search"
    private val DEFAULT_SECTION = WatchlistTabSection.STOCK
  }
}
