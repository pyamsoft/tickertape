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
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.isOption
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.asEditData
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
    quoteInteractor: QuoteInteractor,
    thisHoldingType: HoldingType,
) :
    UiSavedStateViewModel<SymbolAddViewState, SymbolAddControllerEvent>(
        savedState,
        SymbolAddViewState(
            error = null,
            quote = null,
            query = "".asEditData(),
            searchResults = emptyList<SearchResult>().pack(),
            type = thisHoldingType,
        )) {

  private val quoteFetcher =
      highlander<ResultWrapper<StockQuote>, StockSymbol> { symbol ->
        quoteInteractor.getQuotes(false, listOf(symbol)).map { it.first() }.map {
          it.quote.requireNotNull()
        }
      }

  private var searchJob: Job? = null

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      val symbol = restoreSavedState(KEY_SYMBOL) { "" }
      setState(
          stateChange = { copy(query = symbol.asEditData(true), quote = null) },
          andThen = { newState -> performLookup(newState.query.text) })
    }

    doOnCleared { resetSearchJob() }
  }

  private fun resetSearchJob() {
    searchJob?.cancel()
    searchJob = null
  }

  fun handleLookupSymbol(symbol: String) {
    setState(
        stateChange = { copy(query = symbol.asEditData()) },
        andThen = { newState ->
          val newSymbol = newState.query
          putSavedState(KEY_SYMBOL, newSymbol.text)
          performLookup(newSymbol.text)
        })
  }

  private fun CoroutineScope.performLookup(query: String) {
    resetSearchJob()

    searchJob =
        launch(context = Dispatchers.Default) {
          val currentType = state.type

          interactor
              .search(false, query)
              .map { results ->
                results.filter { item ->
                  val type = item.type()
                  return@filter when (currentType) {
                    is HoldingType.Stock -> !NOT_STOCK_TYPES.contains(type)
                    is HoldingType.Crypto -> type === EquityType.CRYPTO
                    is HoldingType.Options.Buy, is HoldingType.Options.Sell ->
                        type == EquityType.OPTION
                  }
                }
              }
              .onSuccess { Timber.d("Search results: $it") }
              .onSuccess { setState { copy(searchResults = it.pack()) } }
              .onFailure { Timber.e(it, "Search failed") }
              .onFailure { setState { copy(searchResults = it.packError()) } }
        }
  }

  fun handleUpdateOptionSide() {
    val currentType = state.type
    if (!currentType.isOption()) {
      Timber.w("Cannot update type when not an option type: $currentType ")
      return
    }
    setState(
        stateChange = {
          copy(
              type =
                  if (currentType == HoldingType.Options.Buy) HoldingType.Options.Sell
                  else HoldingType.Options.Buy)
        },
        andThen = { newState -> performLookup(newState.query.text) })
  }

  fun handleResultSelected(index: Int) {
    val data = state.searchResults
    if (data !is PackedData.Data<List<SearchResult>>) {
      Timber.w("Cannot handle result selected in error state: $data")
      return
    }

    val result = data.value[index]
    Timber.d("Result selected: $result")
    // We have to clear first to reset the delegate, and then we re-publish with the text
    val symbol = result.symbol()
          setState(
              stateChange = { copy(query = symbol.symbol().asEditData(true)) },
              andThen = {
                quoteFetcher
                    .call(symbol)
                    .onSuccess { setState { copy(quote = it) } }
                    .onFailure { Timber.e(it, "Failed to lookup stock quote: $symbol") }
                    .onFailure { setState { copy(quote = null) } }
              })
  }

  fun handleCommitSymbol() {
    val quote = state.quote
    if (quote == null) {
      Timber.w("Cannot commit symbol without quote: $quote")
      return
    }

    setState(stateChange = { copy(query = "".asEditData(true), quote = null) }, andThen = { onCommitSymbol(quote) })
  }

  protected abstract suspend fun onCommitSymbol(stock: StockQuote)

  companion object {

    private val NOT_STOCK_TYPES =
        arrayOf(
            EquityType.CRYPTO,
            EquityType.OPTION,
        )
    private const val KEY_SYMBOL = "symbol"
  }
}
