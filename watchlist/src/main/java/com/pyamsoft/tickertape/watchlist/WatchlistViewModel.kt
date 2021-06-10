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
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.onActualError
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.db.symbol.SymbolChangeEvent
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.ui.BottomOffset
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
    private val bottomOffsetBus: EventConsumer<BottomOffset>
) :
    UiViewModel<WatchListViewState, WatchListControllerEvent>(
        initialState =
            WatchListViewState(
                error = null, isLoading = false, quotes = emptyList(), bottomOffset = 0)) {

  private val quoteFetcher =
      highlander<Unit, Boolean> { force ->
        setState(
            stateChange = { copy(isLoading = true) },
            andThen = {
              try {
                val quotes = interactor.getQuotes(force)
                setState { copy(error = null, quotes = quotes, isLoading = false) }
              } catch (error: Throwable) {
                error.onActualError { e ->
                  Timber.e(e, "Failed to fetch quotes")
                  setState { copy(error = e, isLoading = false) }
                }
              }
            })
      }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      bottomOffsetBus.onEvent { setState { copy(bottomOffset = it.height) } }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
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
    Timber.d("New symbol inserted: $symbol")

    // Don't actually insert anything to the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    fetchQuotes(true)
  }

  private fun CoroutineScope.handleUpdateSymbol(symbol: StockSymbol) {
    Timber.d("Existing symbol updated: $symbol")

    // Don't actually update anything in the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    fetchQuotes(true)
  }

  private fun CoroutineScope.handleDeleteSymbol(symbol: StockSymbol, offerUndo: Boolean) {
    setState { copy(quotes = quotes.filterNot { it.symbol.symbol() == symbol.symbol() }) }
    // TODO offer up undo ability

    // On delete, we don't need to re-fetch quotes from the network
  }

  fun handleFetchQuotes(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchQuotes(force) }
  }

  private fun CoroutineScope.fetchQuotes(force: Boolean) {
    launch(context = Dispatchers.Default) {
        quoteFetcher.call(force)

        // After the quotes are fetched, start the tape
        tapeLauncher.start()
    }
  }

  fun handleRemove(index: Int) {
      viewModelScope.launch(context = Dispatchers.Default) {
          val quote = state.quotes[index]
          interactor.removeQuote(quote.symbol)
      }
  }
}
