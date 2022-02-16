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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.core.ActivityScope
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.quote.Ticker
import javax.inject.Inject

interface BaseState : UiViewState

interface HomePortfolioViewState : UiViewState {
  val isLoadingPortfolio: Boolean
  val portfolio: PortfolioStockList
  val portfolioError: Throwable?
}

interface HomeWatchListViewState : BaseState {
  val isLoadingWatchlist: Boolean
  val watchlist: List<Ticker>
  val watchlistError: Throwable?
}

interface HomeIndexesViewState : BaseState {
  val isLoadingIndexes: Boolean
  val indexes: List<Ticker>
  val indexesError: Throwable?
}

interface HomeGainersViewState : BaseState {
  val isLoadingGainers: Boolean
  val gainers: List<Ticker>
  val gainersError: Throwable?
}

interface HomeLosersViewState : BaseState {
  val isLoadingLosers: Boolean
  val losers: List<Ticker>
  val losersError: Throwable?
}

interface HomeTrendingViewState : BaseState {
  val isLoadingTrending: Boolean
  val trending: List<Ticker>
  val trendingError: Throwable?
}

interface HomeShortedViewState : BaseState {
  val isLoadingMostShorted: Boolean
  val mostShorted: List<Ticker>
  val mostShortedError: Throwable?
}

interface HomeGrowthTechViewState : BaseState {
  val isLoadingGrowthTech: Boolean
  val growthTech: List<Ticker>
  val growthTechError: Throwable?
}

interface HomeUndervaluedGrowthViewState : BaseState {
  val isLoadingUndervaluedGrowth: Boolean
  val undervaluedGrowth: List<Ticker>
  val undervaluedGrowthError: Throwable?
}

interface HomeMostActiveViewState : BaseState {
  val isLoadingMostActive: Boolean
  val mostActive: List<Ticker>
  val mostActiveError: Throwable?
}

interface HomeViewState :
    UiViewState,
    HomePortfolioViewState,
    HomeWatchListViewState,
    HomeIndexesViewState,
    HomeGainersViewState,
    HomeLosersViewState,
    HomeTrendingViewState,
    HomeShortedViewState,
    HomeUndervaluedGrowthViewState,
    HomeGrowthTechViewState,
    HomeMostActiveViewState {
  val isLoading: Boolean
}

@ActivityScope
internal class MutableHomeViewState @Inject internal constructor() : HomeViewState {
  override var isLoading by mutableStateOf(false)

  override var isLoadingPortfolio by mutableStateOf(false)
  override var portfolio by mutableStateOf(PortfolioStockList.empty())
  override var portfolioError by mutableStateOf<Throwable?>(null)

  override var isLoadingWatchlist by mutableStateOf(false)
  override var watchlist by mutableStateOf(emptyList<Ticker>())
  override var watchlistError by mutableStateOf<Throwable?>(null)

  override var isLoadingIndexes by mutableStateOf(false)
  override var indexes by mutableStateOf(emptyList<Ticker>())
  override var indexesError by mutableStateOf<Throwable?>(null)

  override var isLoadingGainers by mutableStateOf(false)
  override var gainers by mutableStateOf(emptyList<Ticker>())
  override var gainersError by mutableStateOf<Throwable?>(null)

  override var isLoadingLosers by mutableStateOf(false)
  override var losers by mutableStateOf(emptyList<Ticker>())
  override var losersError by mutableStateOf<Throwable?>(null)

  override var isLoadingTrending by mutableStateOf(false)
  override var trending by mutableStateOf(emptyList<Ticker>())
  override var trendingError by mutableStateOf<Throwable?>(null)

  override var isLoadingMostShorted by mutableStateOf(false)
  override var mostShorted by mutableStateOf(emptyList<Ticker>())
  override var mostShortedError by mutableStateOf<Throwable?>(null)

  override var isLoadingGrowthTech by mutableStateOf(false)
  override var growthTech by mutableStateOf(emptyList<Ticker>())
  override var growthTechError by mutableStateOf<Throwable?>(null)

  override var isLoadingUndervaluedGrowth by mutableStateOf(false)
  override var undervaluedGrowth by mutableStateOf(emptyList<Ticker>())
  override var undervaluedGrowthError by mutableStateOf<Throwable?>(null)

  override var isLoadingMostActive by mutableStateOf(false)
  override var mostActive by mutableStateOf(emptyList<Ticker>())
  override var mostActiveError by mutableStateOf<Throwable?>(null)
}
