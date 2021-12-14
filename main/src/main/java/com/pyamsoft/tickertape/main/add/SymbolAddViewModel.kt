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

import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import kotlinx.coroutines.Job

abstract class SymbolAddViewModel
protected constructor(
    savedState: UiSavedState,
    private val interactor: SymbolAddInteractor,
    tickerInteractor: TickerInteractor,
    private val equityType: EquityType,
    side: TradeSide,
) :
    UiSavedStateViewModel<SymbolAddViewState, SymbolAddControllerEvent>(
        savedState,
        SymbolAddViewState(
            error = null,
            quote = null,
            query = "",
            searchResults = emptyList<SearchResult>(),
            side = side,
        )) {

  private val quoteFetcher =
      highlander<ResultWrapper<StockQuote>, StockSymbol> { symbol ->
        tickerInteractor.getQuotes(false, listOf(symbol)).map { it.first() }.map {
          it.quote.requireNotNull()
        }
      }

  private var searchJob: Job? = null

  init {
    doOnCleared { resetSearchJob() }
  }

  private fun resetSearchJob() {
    searchJob?.cancel()
    searchJob = null
  }

  fun handleLookupSymbol(symbol: String) {}

  fun handleUpdateOptionSide() {}

  fun handleResultSelected(index: Int) {}

  fun handleSearchTriggered() {}

  fun handleCommitSymbol() {}

  protected abstract suspend fun onCommitSymbol(stock: StockQuote)
}
