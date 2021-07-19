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
import com.pyamsoft.tickertape.ui.UiScrollingContainer
import javax.inject.Inject

class HomeScrollContainer
@Inject
internal constructor(
    parent: ViewGroup,
    nestedPortfolio: HomePortfolio,
    nestedWatchlistTitle: HomeWatchlistTitle,
    nestedWatchlist: HomeWatchlist,
    nestedIndexes: HomeIndexList,
    nestedTrending: HomeTrendingList,
    nestedGainers: HomeGainerList,
    nestedLosers: HomeLoserList,
    nestedBottomSpacer: HomeBottomSpacer
) : UiScrollingContainer<HomeViewState, HomeViewEvent>(parent) {

  init {
    nest(
        nestedPortfolio,
        nestedWatchlistTitle,
        nestedWatchlist,
        nestedIndexes,
        nestedTrending,
        nestedGainers,
        nestedLosers,
        nestedBottomSpacer,
    )
  }
}
