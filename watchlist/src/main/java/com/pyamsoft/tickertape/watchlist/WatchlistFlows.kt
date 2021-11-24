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
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError

// Public constructor, used in home module
data class WatchListViewState(
    val embedded: Boolean,
    val query: String,
    val section: WatchlistTabSection,
    val isLoading: Boolean,
    val watchlist: PackedData<List<Ticker>>,
    val topOffset: Int,
    val bottomOffset: Int,
) : UiViewState {

  val displayWatchlist: PackedData<List<DisplayWatchlist>> =
      when (watchlist) {
        is PackedData.Data -> {
          val list = watchlist.value
          val currentSearch = query
          val allItems =
              list.asSequence()
                  .filter { qs ->
                    val symbol = qs.symbol.symbol()
                    val name = qs.quote?.company()?.company()
                    return@filter if (symbol.contains(currentSearch, ignoreCase = true)) true
                    else name?.contains(currentSearch, ignoreCase = true) ?: false
                  }
                  .map { DisplayWatchlist.Item(it) }
                  .toList()
          allItems.pack()
        }
        is PackedData.Error -> watchlist.throwable.packError()
      }

  sealed class DisplayWatchlist {

    data class Item internal constructor(val stock: Ticker) : DisplayWatchlist()
  }
}

sealed class WatchListViewEvent : UiViewEvent {

  data class Search internal constructor(val query: String) : WatchListViewEvent()

  object ForceRefresh : WatchListViewEvent()

  data class Select internal constructor(val index: Int) : WatchListViewEvent()

  data class Remove internal constructor(val index: Int) : WatchListViewEvent()

  object ShowStocks : WatchListViewEvent()

  object ShowOptions : WatchListViewEvent()

  object ShowCrypto : WatchListViewEvent()
}

sealed class WatchListControllerEvent : UiControllerEvent {

  data class ManageSymbol internal constructor(val quote: Ticker) : WatchListControllerEvent()

  data class AddNewSymbol
  internal constructor(
      val type: EquityType,
      val side: TradeSide,
  ) : WatchListControllerEvent()
}
