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

import android.graphics.Color
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
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
    appBarActivity: AppBarActivity,
    owner: LifecycleOwner,
    factory: WatchlistItemComponent.Factory,
) :
    BaseWatchlistList<WatchListViewState, WatchListViewEvent>(
        parent,
        owner,
        appBarActivity,
        factory,
    ) {

  private var bottomDecoration: RecyclerView.ItemDecoration? = null

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
    }

    doOnTeardown {
      binding.watchlistList.removeAllItemDecorations()
      bottomDecoration = null
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
    handleRender(state, includeAppBarSpacer = true)
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomOffset(it) }
  }

  private fun handleBottomOffset(height: Int) {
    bottomDecoration?.also { binding.watchlistList.removeItemDecoration(it) }
    bottomDecoration =
        LinearBoundsMarginDecoration(bottomMargin = (height * 1.5).toInt()).apply {
      binding.watchlistList.addItemDecoration(this)
    }
  }
}
