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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.symbol.SymbolChangeEvent
import com.pyamsoft.tickertape.main.MainAdderViewModel
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.ui.AddNew
import com.pyamsoft.tickertape.ui.BottomOffset
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError
import com.pyamsoft.tickertape.ui.transformData
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistViewModel
@Inject
internal constructor(
    private val tapeLauncher: TapeLauncher,
    private val interactor: WatchlistInteractor,
    private val bottomOffsetBus: EventConsumer<BottomOffset>,
    addNewBus: EventConsumer<AddNew>
) :
    MainAdderViewModel<WatchListViewState, WatchListControllerEvent>(
        addNewBus = addNewBus,
        initialState =
            WatchListViewState(
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
  }

  override fun CoroutineScope.onAddNewEvent() {
    Timber.d("Watchlist add new symbol!")
    publish(WatchListControllerEvent.AddNewSymbol)
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
                  val type = qs.quote?.type() ?: return@filter false
                  return@filter when (currentSection) {
                    WatchlistTabSection.STOCK -> !CATCH_ALL_TYPE.contains(type)
                    WatchlistTabSection.OPTION -> type == EquityType.OPTION
                    WatchlistTabSection.CRYPTO -> type == EquityType.CRYPTO
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

  fun handleRemove(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val data = state.watchlist
      if (data !is PackedData.Data<List<QuotedStock>>) {
        Timber.w("Cannot remove symbol in error state: $data")
        return@launch
      }

      val quote = data.value[index]
      interactor
          .removeQuote(quote.symbol)
          .onSuccess { Timber.d("Removed quote $quote") }
          .onFailure { Timber.e(it, "Error removing quote: $quote") }
    }
  }

  fun handleDigSymbol(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val data = state.watchlist
      if (data !is PackedData.Data<List<QuotedStock>>) {
        Timber.w("Cannot dig symbol in error state: $data")
        return@launch
      }

      val quote = data.value[index]
      publish(WatchListControllerEvent.ManageSymbol(quote))
    }
  }

  override fun handleShowStocks() {
    setState(
        stateChange = { copy(section = WatchlistTabSection.STOCK) },
        andThen = { fetchQuotes(false) })
  }

  override fun handleShowOptions() {
    setState(
        stateChange = { copy(section = WatchlistTabSection.OPTION) },
        andThen = { fetchQuotes(false) })
  }

  override fun handleShowCrypto() {
    setState(
        stateChange = { copy(section = WatchlistTabSection.CRYPTO) },
        andThen = { fetchQuotes(false) })
  }

  companion object {

    private val CATCH_ALL_TYPE = arrayOf(EquityType.OPTION, EquityType.CRYPTO)
    private val DEFAULT_SECTION = WatchlistTabSection.STOCK
  }
}
