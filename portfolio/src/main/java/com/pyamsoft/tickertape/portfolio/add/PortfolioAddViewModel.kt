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

package com.pyamsoft.tickertape.portfolio.add

import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.tickertape.main.add.SymbolAddInteractor
import com.pyamsoft.tickertape.main.add.SymbolAddViewModel
import com.pyamsoft.tickertape.main.add.SymbolAddViewState
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.StockQuote
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import timber.log.Timber

class PortfolioAddViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val interactor: PortfolioAddInteractor,
    quoteInteractor: QuoteInteractor,
    addInteractor: SymbolAddInteractor,
    thisHoldingType: HoldingType,
) : SymbolAddViewModel(savedState, addInteractor, quoteInteractor, thisHoldingType) {

  override suspend fun onCommitSymbol(stock: StockQuote) {
    val symbol = stock.symbol()
    val type = state.type

    val holdingType =
        when (val equityType = stock.type()) {
          EquityType.OPTION ->
              if (type == HoldingType.Options.Buy || type == HoldingType.Options.Sell) type
              else {
                Timber.w(
                    "Option EquityType and expected HoldingType do not match: $equityType $type")
                setState {
                  copy(error = SymbolAddViewState.TypeMismatchException(equityType, type))
                }
                return
              }
          EquityType.CRYPTO ->
              if (type == HoldingType.Crypto) type
              else {
                Timber.w(
                    "Crypto EquityType and expected HoldingType do not match: $equityType $type")
                setState {
                  copy(error = SymbolAddViewState.TypeMismatchException(equityType, type))
                }
                return
              }
          else ->
              if (type == HoldingType.Stock) type
              else {
                Timber.w(
                    "Stock EquityType and expected HoldingType do not match: $equityType $type")
                setState {
                  copy(error = SymbolAddViewState.TypeMismatchException(equityType, type))
                }
                return
              }
        }

    Timber.d("Commit symbol to DB: $symbol $holdingType")
    interactor
        .commitSymbol(symbol, holdingType)
        .onSuccess { Timber.d("Committed new symbols to db: $symbol $holdingType") }
        .onSuccess { setState { copy(error = null) } }
        .onFailure { Timber.e(it, "Failed to commit symbols to db: $symbol $holdingType") }
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<PortfolioAddViewModel> {
    override fun create(savedState: UiSavedState): PortfolioAddViewModel
  }
}
