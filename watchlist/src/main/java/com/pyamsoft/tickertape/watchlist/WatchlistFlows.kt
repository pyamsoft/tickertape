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

package com.pyamsoft.tickertape.watchlist

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.ui.PackedData

// Public constructor, used in home module
data class WatchListViewState(
    val section: WatchlistTabSection,
    val isLoading: Boolean,
    val watchlist: PackedData<List<QuotedStock>>,
    val bottomOffset: Int,
) : UiViewState

sealed class WatchListViewEvent : UiViewEvent {

  object ForceRefresh : WatchListViewEvent()

  data class Select internal constructor(val index: Int) : WatchListViewEvent()

  data class Remove internal constructor(val index: Int) : WatchListViewEvent()

  object ShowStocks : WatchListViewEvent()

  object ShowOptions : WatchListViewEvent()

  object ShowCrypto : WatchListViewEvent()
}

sealed class WatchListControllerEvent : UiControllerEvent {

  data class ManageSymbol internal constructor(val quote: QuotedStock) : WatchListControllerEvent()

  object AddNewSymbol : WatchListControllerEvent()
}
