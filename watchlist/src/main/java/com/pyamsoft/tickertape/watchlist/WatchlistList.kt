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

package com.pyamsoft.tickertape.watchlist

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.watchlist.item.WatchlistItemComponent
import io.cabriole.decorator.LinearBoundsMarginDecoration
import io.cabriole.decorator.LinearMarginDecoration
import javax.inject.Inject
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class WatchlistList
@Inject
internal constructor(
    parent: ViewGroup,
    owner: LifecycleOwner,
    factory: WatchlistItemComponent.Factory
) : BaseWatchlistList<WatchListViewState, WatchListViewEvent>(parent, owner, factory) {

  init {
    doOnInflate {
      FastScrollerBuilder(binding.watchlistList)
          .useMd2Style()
          .setPopupTextProvider(usingAdapter())
          .build()
    }

    doOnInflate {
      val margin = 16.asDp(binding.watchlistList.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      LinearMarginDecoration.create(margin).apply { binding.watchlistList.addItemDecoration(this) }

      // The bottom has additional space to fit the FAB
      val bottomMargin = 24.asDp(binding.watchlistList.context)
      LinearBoundsMarginDecoration(bottomMargin = bottomMargin).apply {
        binding.watchlistList.addItemDecoration(this)
      }
    }
  }

  override fun onSelect(index: Int) {
    publish(WatchListViewEvent.Select(index))
  }

  override fun onRefresh() {
    publish(WatchListViewEvent.ForceRefresh)
  }

  override fun onRemove(index: Int) {
    publish(WatchListViewEvent.Remove(index))
  }

  override fun onRender(state: UiRender<WatchListViewState>) {
    handleRender(state)
  }
}
