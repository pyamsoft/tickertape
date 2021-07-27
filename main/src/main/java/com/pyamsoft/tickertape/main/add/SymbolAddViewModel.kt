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

package com.pyamsoft.tickertape.main.add

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class SymbolAddViewModel
protected constructor(
    savedState: UiSavedState,
    private val interactor: SymbolAddInteractor,
) :
    UiSavedStateViewModel<SymbolAddViewState, SymbolAddControllerEvent>(
        savedState,
        SymbolAddViewState(
            query = "",
            searchResults = emptyList<SearchResult>().pack(),
            type = HoldingType.Stock)) {

  private var searchJob: Job? = null

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      val symbol = restoreSavedState(KEY_SYMBOL) { "" }
      setState { copy(query = symbol) }
    }

    doOnCleared { resetSearchJob() }
  }

  private fun resetSearchJob() {
    searchJob?.cancel()
    searchJob = null
  }

  fun handleLookupSymbol(symbol: String, filterType: Boolean) {
    setState(
        stateChange = { copy(query = symbol) },
        andThen = { newState ->
          val newSymbol = newState.query
          putSavedState(KEY_SYMBOL, newSymbol)
          performLookup(newSymbol, filterType)
        })
  }

  private fun CoroutineScope.performLookup(query: String, filterType: Boolean) {
    resetSearchJob()

    searchJob =
        launch(context = Dispatchers.Default) {
          interactor
              .search(false, query, if (filterType) state.type else null)
              .onSuccess { Timber.d("Search results: $it") }
              .onSuccess { setState { copy(searchResults = it.pack()) } }
              .onFailure { Timber.e(it, "Search failed") }
              .onFailure { setState { copy(searchResults = it.packError()) } }
        }
  }

  fun handleResultSelected(index: Int) {
    val data = state.searchResults
    if (data !is PackedData.Data<List<SearchResult>>) {
      Timber.w("Cannot handle result selected in error state: $data")
      return
    }

    val result = data.value[index]
    Timber.d("Result selected: $result")
    handleCommitSymbol(result.symbol().symbol(), state.type)
  }

  fun handleCommitSymbol() {
    handleCommitSymbol(state.query, state.type)
  }

  fun handleUpdateType() {
    val newType =
        when (state.type) {
          is HoldingType.Options.Buy -> HoldingType.Options.Sell
          is HoldingType.Options.Sell -> HoldingType.Options.Buy
          is HoldingType.Stock, HoldingType.Crypto -> {
            Timber.w("Cannot update type when type is not Options.")
            return
          }
        }

    setState(
        stateChange = { copy(type = newType) },
        andThen = { newState -> performLookup(newState.query, true) })
  }

  protected abstract fun handleCommitSymbol(symbol: String, type: HoldingType)

  companion object {

    private const val KEY_SYMBOL = "symbol"
  }
}
