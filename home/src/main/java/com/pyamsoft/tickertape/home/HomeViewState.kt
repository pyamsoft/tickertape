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
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.Ticker
import javax.inject.Inject

interface HomePortfolioViewState : UiViewState {
  val portfolio: List<PortfolioStock>
  val portfolioError: Throwable?
}

interface HomeWatchListViewState : UiViewState {
  val watchlist: List<Ticker>
  val watchlistError: Throwable?
}

interface HomeIndexesViewState : UiViewState {
  val indexes: List<Ticker>
  val indexesError: Throwable?
}

interface HomeGainersViewState : UiViewState {
  val gainers: List<Ticker>
  val gainersError: Throwable?
}

interface HomeLosersViewState : UiViewState {
  val losers: List<Ticker>
  val losersError: Throwable?
}

interface HomeTrendingViewState : UiViewState {
  val trending: List<Ticker>
  val trendingError: Throwable?
}

interface HomeShortedViewState : UiViewState {
  val mostShorted: List<Ticker>
  val mostShortedError: Throwable?
}

interface HomeViewState :
    UiViewState,
    HomePortfolioViewState,
    HomeWatchListViewState,
    HomeIndexesViewState,
    HomeGainersViewState,
    HomeLosersViewState,
    HomeTrendingViewState,
    HomeShortedViewState {
  val isLoading: Boolean
}

internal class MutableHomeViewState @Inject internal constructor() : HomeViewState {
  override var isLoading by mutableStateOf(false)
  override var portfolio by mutableStateOf(emptyList<PortfolioStock>())
  override var portfolioError by mutableStateOf<Throwable?>(null)
  override var watchlist by mutableStateOf(emptyList<Ticker>())
  override var watchlistError by mutableStateOf<Throwable?>(null)
  override var indexes by mutableStateOf(emptyList<Ticker>())
  override var indexesError by mutableStateOf<Throwable?>(null)
  override var gainers by mutableStateOf(emptyList<Ticker>())
  override var gainersError by mutableStateOf<Throwable?>(null)
  override var losers by mutableStateOf(emptyList<Ticker>())
  override var losersError by mutableStateOf<Throwable?>(null)
  override var trending by mutableStateOf(emptyList<Ticker>())
  override var trendingError by mutableStateOf<Throwable?>(null)
  override var mostShorted by mutableStateOf(emptyList<Ticker>())
  override var mostShortedError by mutableStateOf<Throwable?>(null)
}
