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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.tickertape.main.add.SymbolAddControllerEvent
import com.pyamsoft.tickertape.main.add.SymbolAddViewModel
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.asSymbols
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PortfolioAddViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val interactor: PortfolioAddInteractor,
    stockInteractor: StockInteractor,
) : SymbolAddViewModel(savedState, stockInteractor) {

  override fun handleCommitSymbol() {
    val symbol = state.symbol
    viewModelScope.launch(context = Dispatchers.Default) {
      Timber.d("Commit symbol to DB: $symbol")
      interactor
          .commitSymbol(symbol.asSymbols())
          .onSuccess { Timber.d("Committed new symbols to db: $symbol") }
          .onFailure { Timber.e(it, "Failed to commit symbols to db: $symbol") }
      publish(SymbolAddControllerEvent.Close)
    }
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<PortfolioAddViewModel> {
    override fun create(savedState: UiSavedState): PortfolioAddViewModel
  }
}
