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
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.watchlist.BaseWatchlistList
import com.pyamsoft.tickertape.watchlist.WatchListViewState
import com.pyamsoft.tickertape.watchlist.item.WatchlistItemComponent
import javax.inject.Inject

class HomeWatchlist
@Inject
internal constructor(
    parent: ViewGroup,
    owner: LifecycleOwner,
    factory: WatchlistItemComponent.Factory
) : BaseWatchlistList<HomeViewState, Nothing>(parent, owner, factory) {

  init {
    doOnInflate {
      layoutRoot.post {
        layoutRoot.updateLayoutParams { this.height = 800.asDp(layoutRoot.context) }
      }
    }

    doOnInflate {
      binding.watchlistList.isNestedScrollingEnabled = false
      binding.watchlistSwipeRefresh.isNestedScrollingEnabled = false
      binding.watchlistSwipeRefresh.isEnabled = false
    }
  }

  override fun onRemove(index: Int) {}

  override fun onSelect(index: Int) {}

  override fun onRefresh() {}

  override fun onRender(state: UiRender<HomeViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
    state.mapChanged { it.watchlist }.render(viewScope) { handleWatchlistChanged(it) }
  }

  private fun handleWatchlistChanged(stocks: List<QuotedStock>) {
    val size = 96.asDp(layoutRoot.context)
    layoutRoot.updateLayoutParams { this.height = stocks.size * size * 2 }
  }

  private fun handleStateChanged(state: HomeViewState) {
    handleRender(
        WatchListViewState(
                error = state.watchlistError,
                isLoading = state.isLoadingWatchlist,
                quotes = state.watchlist,
                bottomOffset = 0)
            .asUiRender())
  }
}
