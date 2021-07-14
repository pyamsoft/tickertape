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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.asUiRender
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.watchlist.BaseWatchlistList
import com.pyamsoft.tickertape.watchlist.WatchListViewState
import com.pyamsoft.tickertape.watchlist.item.WatchlistItemComponent
import io.cabriole.decorator.DecorationLookup
import io.cabriole.decorator.LinearMarginDecoration
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
      binding.watchlistList.isNestedScrollingEnabled = false
      binding.watchlistSwipeRefresh.isNestedScrollingEnabled = false
      binding.watchlistSwipeRefresh.isEnabled = false
    }

    // For some reason the match_parent height does not make this list fill content
    // Either way, we only want the list to be one card high as we will modify it to scroll
    // horizontally
    doOnInflate {
      val size = 152.asDp(layoutRoot.context)
      layoutRoot.updateLayoutParams { this.height = size }
    }

    // Modify the layout manager to scroll horizontal
    doOnInflate {
      val manager = binding.watchlistList.layoutManager
      if (manager is LinearLayoutManager) {
        manager.orientation = RecyclerView.HORIZONTAL
      }
    }

    doOnInflate {
      val margin = 16.asDp(binding.watchlistList.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      //
      // First item is weird.
      LinearMarginDecoration(
              leftMargin = margin * 2,
              rightMargin = margin,
              orientation = RecyclerView.HORIZONTAL,
              decorationLookup =
                  object : DecorationLookup {
                    override fun shouldApplyDecoration(position: Int, itemCount: Int): Boolean {
                      return position == 0
                    }
                  })
          .apply { binding.watchlistList.addItemDecoration(this) }

      LinearMarginDecoration.createHorizontal(
              margin,
              orientation = RecyclerView.HORIZONTAL,
              decorationLookup =
                  object : DecorationLookup {
                    override fun shouldApplyDecoration(position: Int, itemCount: Int): Boolean {
                      return position > 0
                    }
                  })
          .apply { binding.watchlistList.addItemDecoration(this) }

      LinearMarginDecoration.createVertical(margin, orientation = RecyclerView.HORIZONTAL).apply {
        binding.watchlistList.addItemDecoration(this)
      }
    }
  }

  override fun onRemove(index: Int) {}

  override fun onSelect(index: Int) {}

  override fun onRefresh() {}

  override fun onRender(state: UiRender<HomeViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: HomeViewState) {
    handleRender(
        WatchListViewState(
                isLoading = state.isLoadingWatchlist, watchlist = state.watchlist, bottomOffset = 0)
            .asUiRender())
  }
}
