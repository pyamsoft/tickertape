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

package com.pyamsoft.tickertape.home

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.ui.PackedData

data class HomeViewState
internal constructor(
    internal val portfolio: PackedData<PortfolioStockList>,
    internal val isLoadingPortfolio: Boolean,
    internal val watchlist: PackedData<List<QuotedStock>>,
    internal val isLoadingWatchlist: Boolean,
    val indexes: PackedData<List<QuotedChart>>,
    val isLoadingIndexes: Boolean,
    val gainers: PackedData<List<TopDataWithChart>>,
    val isLoadingGainers: Boolean,
    val losers: PackedData<List<TopDataWithChart>>,
    val isLoadingLosers: Boolean,
    val trending: PackedData<List<TopDataWithChart>>,
    val isLoadingTrending: Boolean,
    val mostShorted: PackedData<List<TopDataWithChart>>,
    val isLoadingMostShorted: Boolean,
    val bottomOffset: Int,
) : UiViewState {

  data class RenderState<S> internal constructor(val data: PackedData<S>, val isLoading: Boolean)

  val portfolioState = RenderState(data = portfolio, isLoading = isLoadingPortfolio)
  val watchlistState = RenderState(data = watchlist, isLoading = isLoadingWatchlist)
}

sealed class HomeViewEvent : UiViewEvent {

  data class DigDeeperWatchlist internal constructor(val index: Int) : HomeViewEvent()

  object OpenWatchlist : HomeViewEvent()

  object OpenPortfolio : HomeViewEvent()
}

sealed class HomeControllerEvent : UiControllerEvent {

  data class ManageWatchlistSymbol internal constructor(val quote: QuotedStock) :
      HomeControllerEvent()
}
