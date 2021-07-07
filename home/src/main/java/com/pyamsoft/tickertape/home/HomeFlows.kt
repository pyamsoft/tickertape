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
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.StockChart

data class HomeViewState
internal constructor(
    val portfolio: List<PortfolioStock>,
    val isLoadingPortfolio: Boolean,
    val portfolioError: Throwable?,
    val watchlist: List<QuotedStock>,
    val isLoadingWatchlist: Boolean,
    val watchlistError: Throwable?,
    val indexes: List<QuotedChart>,
    val isLoadingIndexes: Boolean,
    val indexesError: Throwable?,
    val bottomOffset: Int,
) : UiViewState

sealed class HomeViewEvent : UiViewEvent

sealed class HomeControllerEvent : UiControllerEvent
