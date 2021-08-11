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

package com.pyamsoft.tickertape.main

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class MainAdderViewModel<S : UiViewState, C : UiControllerEvent>
protected constructor(
    savedState: UiSavedState,
    private val addNewBus: EventConsumer<AddNew>,
    initialState: S
) : UiSavedStateViewModel<S, C>(savedState, initialState) {

  protected abstract fun CoroutineScope.onAddNewEvent(type: EquityType, side: TradeSide)

  abstract fun handleShowStocks()

  abstract fun handleShowOptions()

  abstract fun handleShowCrypto()

  fun handleListenForAddEvents(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Default) {
      addNewBus.onEvent { onAddNewEvent(it.type, it.side) }
    }
  }
}
