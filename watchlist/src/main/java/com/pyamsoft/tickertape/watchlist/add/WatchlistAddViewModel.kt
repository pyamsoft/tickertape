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

package com.pyamsoft.tickertape.watchlist.add

import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.tickertape.main.add.SymbolAddInteractor
import com.pyamsoft.tickertape.main.add.SymbolAddViewModel
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.TradeSide
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import timber.log.Timber

class WatchlistAddViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val interactor: WatchlistAddInteractor,
    addInteractor: SymbolAddInteractor,
    tickerInteractor: TickerInteractor,
    equityType: EquityType,
    tradeSide: TradeSide,
) :
    SymbolAddViewModel(
        savedState,
        addInteractor,
        tickerInteractor,
        equityType,
        tradeSide,
    ) {

  override suspend fun onCommitSymbol(stock: StockQuote) {
    val symbol = stock.symbol()
    Timber.d("Commit symbol to DB: $symbol")
    interactor.commitSymbol(symbol).onSuccess { Timber.d("Committed symbols: $symbol") }.onFailure {
      Timber.e(it, "Error committing symbols: $symbol")
    }
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<WatchlistAddViewModel> {
    override fun create(savedState: UiSavedState): WatchlistAddViewModel
  }
}
