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

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.UiEditTextDelegate

data class SymbolAddViewState
internal constructor(
    val error: Throwable?,
    val quote: StockQuote?,
    val query: UiEditTextDelegate.Data,
    val searchResults: PackedData<List<SearchResult>>,
    val side: TradeSide,
) : UiViewState

sealed class SymbolAddViewEvent : UiViewEvent {

  data class UpdateSymbol(val symbol: String) : SymbolAddViewEvent()

  data class SelectResult internal constructor(val index: Int) : SymbolAddViewEvent()

  object UpdateOptionSide : SymbolAddViewEvent()

  object Close : SymbolAddViewEvent()

  object CommitSymbol : SymbolAddViewEvent()

  object TriggerSearch : SymbolAddViewEvent()
}

sealed class SymbolAddControllerEvent : UiControllerEvent {

  object Close : SymbolAddControllerEvent()
}
