/*
 * Copyright 2023 pyamsoft
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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.portfolio.PortfolioData
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface HomeBaseViewState : UiViewState {

  @Stable
  @Immutable
  enum class LoadingState {
    NONE,
    LOADING,
    DONE
  }
}

@Stable
interface HomePortfolioViewState : HomeBaseViewState {
  val isLoadingPortfolio: StateFlow<HomeBaseViewState.LoadingState>
  val portfolio: StateFlow<PortfolioData?>
  val portfolioError: StateFlow<Throwable?>
}

@Stable
interface HomeIndexesViewState : HomeBaseViewState {
  val isLoadingIndexes: StateFlow<HomeBaseViewState.LoadingState>
  val indexes: StateFlow<List<HomeStock>>
  val indexesError: StateFlow<Throwable?>
}

@Stable
interface HomeGainersViewState : HomeBaseViewState {
  val isLoadingGainers: StateFlow<HomeBaseViewState.LoadingState>
  val gainers: StateFlow<List<HomeStock>>
  val gainersError: StateFlow<Throwable?>
}

@Stable
interface HomeLosersViewState : HomeBaseViewState {
  val isLoadingLosers: StateFlow<HomeBaseViewState.LoadingState>
  val losers: StateFlow<List<HomeStock>>
  val losersError: StateFlow<Throwable?>
}

@Stable
interface HomeTrendingViewState : HomeBaseViewState {
  val isLoadingTrending: StateFlow<HomeBaseViewState.LoadingState>
  val trending: StateFlow<List<HomeStock>>
  val trendingError: StateFlow<Throwable?>
}

@Stable
interface HomeShortedViewState : HomeBaseViewState {
  val isLoadingMostShorted: StateFlow<HomeBaseViewState.LoadingState>
  val mostShorted: StateFlow<List<HomeStock>>
  val mostShortedError: StateFlow<Throwable?>
}

@Stable
interface HomeGrowthTechViewState : HomeBaseViewState {
  val isLoadingGrowthTech: StateFlow<HomeBaseViewState.LoadingState>
  val growthTech: StateFlow<List<HomeStock>>
  val growthTechError: StateFlow<Throwable?>
}

@Stable
interface HomeUndervaluedGrowthViewState : HomeBaseViewState {
  val isLoadingUndervaluedGrowth: StateFlow<HomeBaseViewState.LoadingState>
  val undervaluedGrowth: StateFlow<List<HomeStock>>
  val undervaluedGrowthError: StateFlow<Throwable?>
}

@Stable
interface HomeMostActiveViewState : HomeBaseViewState {
  val isLoadingMostActive: StateFlow<HomeBaseViewState.LoadingState>
  val mostActive: StateFlow<List<HomeStock>>
  val mostActiveError: StateFlow<Throwable?>
}

@Stable
interface HomeViewState :
    UiViewState,
    HomePortfolioViewState,
    HomeIndexesViewState,
    HomeGainersViewState,
    HomeLosersViewState,
    HomeTrendingViewState,
    HomeShortedViewState,
    HomeUndervaluedGrowthViewState,
    HomeGrowthTechViewState,
    HomeMostActiveViewState {
  val isSettingsOpen: StateFlow<Boolean>
}

@Stable
class MutableHomeViewState @Inject internal constructor() : HomeViewState {

  override val isSettingsOpen = MutableStateFlow(false)

  override val isLoadingPortfolio = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val portfolio = MutableStateFlow<PortfolioData?>(null)
  override val portfolioError = MutableStateFlow<Throwable?>(null)

  override val isLoadingIndexes = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val indexes = MutableStateFlow(emptyList<HomeStock>())
  override val indexesError = MutableStateFlow<Throwable?>(null)

  override val isLoadingGainers = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val gainers = MutableStateFlow(emptyList<HomeStock>())
  override val gainersError = MutableStateFlow<Throwable?>(null)

  override val isLoadingLosers = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val losers = MutableStateFlow(emptyList<HomeStock>())
  override val losersError = MutableStateFlow<Throwable?>(null)

  override val isLoadingTrending = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val trending = MutableStateFlow(emptyList<HomeStock>())
  override val trendingError = MutableStateFlow<Throwable?>(null)

  override val isLoadingMostShorted = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val mostShorted = MutableStateFlow(emptyList<HomeStock>())
  override val mostShortedError = MutableStateFlow<Throwable?>(null)

  override val isLoadingGrowthTech = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val growthTech = MutableStateFlow(emptyList<HomeStock>())
  override val growthTechError = MutableStateFlow<Throwable?>(null)

  override val isLoadingUndervaluedGrowth = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val undervaluedGrowth = MutableStateFlow(emptyList<HomeStock>())
  override val undervaluedGrowthError = MutableStateFlow<Throwable?>(null)

  override val isLoadingMostActive = MutableStateFlow(HomeBaseViewState.LoadingState.NONE)
  override val mostActive = MutableStateFlow(emptyList<HomeStock>())
  override val mostActiveError = MutableStateFlow<Throwable?>(null)
}
