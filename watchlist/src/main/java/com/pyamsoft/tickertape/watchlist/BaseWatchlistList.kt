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
import androidx.annotation.CheckResult
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.getUserMessage
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistBinding
import com.pyamsoft.tickertape.watchlist.item.WatchlistItemAdapter
import com.pyamsoft.tickertape.watchlist.item.WatchlistItemComponent
import com.pyamsoft.tickertape.watchlist.item.WatchlistItemViewState
import io.cabriole.decorator.LinearBoundsMarginDecoration
import timber.log.Timber

abstract class BaseWatchlistList<S : UiViewState, V : UiViewEvent>
protected constructor(
    parent: ViewGroup,
    owner: LifecycleOwner,
    factory: WatchlistItemComponent.Factory
) :
    BaseUiView<S, V, WatchlistBinding>(parent),
    SwipeRefreshLayout.OnRefreshListener,
    WatchlistItemAdapter.Callback {

  final override val viewBinding = WatchlistBinding::inflate

  final override val layoutRoot by boundView { watchlistSwipeRefresh }

  private var modelAdapter: WatchlistItemAdapter? = null

  private var bottomDecoration: RecyclerView.ItemDecoration? = null
  private var lastScrollPosition = 0

  init {
    doOnInflate {
      binding.watchlistList.layoutManager =
          LinearLayoutManager(binding.watchlistList.context).apply {
        isItemPrefetchEnabled = true
        initialPrefetchItemCount = 3
      }
    }

    doOnInflate {
      modelAdapter = WatchlistItemAdapter.create(factory, owner, this)
      binding.watchlistList.adapter = modelAdapter
    }

    doOnInflate { binding.watchlistSwipeRefresh.setOnRefreshListener(this) }

    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = binding.watchlistList.layoutManager
      if (manager is LinearLayoutManager) {
        val position = manager.findFirstVisibleItemPosition()
        if (position > 0) {
          outState.put(LAST_SCROLL_POSITION, position)
          return@doOnSaveState
        }
      }

      outState.remove<Nothing>(LAST_SCROLL_POSITION)
    }

    doOnTeardown {
      binding.watchlistList.removeAllItemDecorations()
      bottomDecoration = null
    }

    doOnTeardown {
      binding.watchlistList.adapter = null

      binding.watchlistSwipeRefresh.setOnRefreshListener(null)
      binding.watchlistError.text = ""

      modelAdapter = null
    }
  }

  @CheckResult
  protected fun usingAdapter(): WatchlistItemAdapter {
    return requireNotNull(modelAdapter)
  }

  protected fun handleRender(state: UiRender<WatchListViewState>) {
    state.mapChanged { it.watchlist }.render(viewScope) { handleWatchlist(it) }
    state.mapChanged { it.isLoading }.render(viewScope) { handleLoading(it) }
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomOffset(it) }
  }

  private fun handleWatchlist(watchlist: PackedData<List<QuotedStock>>) {
    return when (watchlist) {
      is PackedData.Data -> handleList(watchlist.value)
      is PackedData.Error -> handleError(watchlist.throwable)
    }
  }

  private fun handleError(throwable: Throwable) {
    binding.apply {
      watchlistList.isGone = true
      watchlistEmpty.isGone = true
      watchlistError.isVisible = true

      watchlistError.text = throwable.getUserMessage()
    }
  }

  private fun handleBottomOffset(height: Int) {
    // Add additional padding to the list bottom to account for the height change in MainContainer
    bottomDecoration?.also { binding.watchlistList.removeItemDecoration(it) }
    bottomDecoration =
        LinearBoundsMarginDecoration(bottomMargin = height * 2).apply {
      binding.watchlistList.addItemDecoration(this)
    }
  }

  private fun setList(list: List<QuotedStock>) {
    val data = list.map { WatchlistItemViewState(symbol = it.symbol, quote = it.quote) }
    Timber.d("Submit data list: $data")

    usingAdapter().submitList(data)

    binding.apply {
      watchlistError.isGone = true
      watchlistEmpty.isGone = true
      watchlistList.isVisible = true
    }
  }

  private fun clearList() {
    usingAdapter().submitList(null)

    binding.apply {
      watchlistError.isGone = true
      watchlistList.isGone = true
      watchlistEmpty.isVisible = true
    }
  }

  private fun handleLoading(loading: Boolean) {
    binding.watchlistSwipeRefresh.apply {
      if (isEnabled) {
        isRefreshing = loading
      }
    }
  }

  private fun handleList(schedule: List<QuotedStock>) {
    if (schedule.isEmpty()) {
      clearList()
    } else {
      setList(schedule)
    }
  }

  companion object {
    private const val LAST_SCROLL_POSITION = "watchlist_last_scroll_position"
  }
}