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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.tickertape.home.databinding.HomeWatchlistTitleBinding
import javax.inject.Inject

class HomeWatchlistTitle
@Inject
internal constructor(
    parent: ViewGroup,
) : BaseUiView<HomeViewState, Nothing, HomeWatchlistTitleBinding>(parent) {

  override val layoutRoot by boundView { homeWatchlistTitle }

  override val viewBinding = HomeWatchlistTitleBinding::inflate

  init {
    doOnInflate { binding.homeWatchlistTitle.text = "My Watchlist Top 5" }

    doOnTeardown { binding.homeWatchlistTitle.text = null }
  }
}