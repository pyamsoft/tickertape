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
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.asSymbol
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import timber.log.Timber

class PortfolioAddViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val interactor: PortfolioAddInteractor,
    addInteractor: SymbolAddInteractor,
    thisHoldingType: HoldingType,
) : SymbolAddViewModel(savedState, addInteractor, thisHoldingType) {

  override suspend fun onCommitSymbol(symbol: String) {
    val type = state.type

    Timber.d("Commit symbol to DB: $symbol")
    interactor
        .commitSymbol(symbol.asSymbol(), type)
        .onSuccess { Timber.d("Committed new symbols to db: $symbol") }
        .onFailure { Timber.e(it, "Failed to commit symbols to db: $symbol") }
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<PortfolioAddViewModel> {
    override fun create(savedState: UiSavedState): PortfolioAddViewModel
  }
}
