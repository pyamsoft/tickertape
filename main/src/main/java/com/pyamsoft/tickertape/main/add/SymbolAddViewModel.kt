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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class SymbolAddViewModel
protected constructor(
    savedState: UiSavedState,
) :
    UiSavedStateViewModel<SymbolAddViewState, SymbolAddControllerEvent>(
        savedState, SymbolAddViewState(symbol = "")) {

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      val symbol = restoreSavedState(KEY_SYMBOL) { "" }
      setState { copy(symbol = symbol) }
    }
  }

  fun handleLookupSymbol(symbol: String) {
    setState(
        stateChange = { copy(symbol = symbol) },
        andThen = { newState ->
          val newSymbol = newState.symbol
          putSavedState(KEY_SYMBOL, newSymbol)
          Timber.d("Lookup symbol search: $newSymbol")
        })
  }

  abstract fun handleCommitSymbol()

  companion object {

    private const val KEY_SYMBOL = "symbol"
  }
}
