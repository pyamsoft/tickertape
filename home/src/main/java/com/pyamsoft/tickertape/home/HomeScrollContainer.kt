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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.ui.UiSwipeRefreshContainer
import javax.inject.Inject

class HomeScrollContainer
@Inject
internal constructor(
    parent: ViewGroup,
    spacer: HomeSpacer,
    portfolio: HomePortfolio,
    watchlistTitle: HomeWatchlistTitle,
    watchlist: HomeWatchlist,
    indexes: HomeIndexList,
    trending: HomeTrendingList,
    gainers: HomeGainerList,
    losers: HomeLoserList,
    mostShorted: HomeMostShortedList,
    bottomSpacer: HomeBottomSpacer
) : UiSwipeRefreshContainer<HomeViewState, HomeViewEvent>(parent) {

  init {
    nest(
        spacer,
        portfolio,
        watchlistTitle,
        watchlist,
        indexes,
        trending,
        gainers,
        losers,
        mostShorted,
        bottomSpacer,
    )

    doOnInflate {
      // Offset the refresh indicator so it appears where we expect it to on page
      // start is the "start location"
      // end is the "pulldown distance"
      binding.containerSwipeRefresh.setProgressViewOffset(false, 200, 400)
    }

    doOnInflate {
      binding.containerSwipeRefresh.setOnRefreshListener { publish(HomeViewEvent.Refresh) }
    }

    doOnTeardown { binding.containerSwipeRefresh.setOnRefreshListener(null) }
  }

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.isLoading }.render(viewScope) { handleLoading(it) }
  }
}
