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
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.quote.QuoteAdapter
import com.pyamsoft.tickertape.quote.QuoteComponent
import com.pyamsoft.tickertape.quote.QuotePair
import com.pyamsoft.tickertape.quote.QuoteViewState
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistBinding
import io.cabriole.decorator.LinearBoundsMarginDecoration
import io.cabriole.decorator.LinearMarginDecoration
import javax.inject.Inject
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import timber.log.Timber

class WatchList
@Inject
internal constructor(parent: ViewGroup, owner: LifecycleOwner, factory: QuoteComponent.Factory) :
    BaseUiView<WatchListViewState, WatchListViewEvent, WatchlistBinding>(parent),
    SwipeRefreshLayout.OnRefreshListener,
    QuoteAdapter.Callback {

  override val viewBinding = WatchlistBinding::inflate

  override val layoutRoot by boundView { watchlistRoot }

  private var modelAdapter: QuoteAdapter? = null

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
      modelAdapter = QuoteAdapter.create(factory, owner, this)
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
      if (manager is GridLayoutManager) {
        val position = manager.findFirstVisibleItemPosition()
        if (position > 0) {
          outState.put(LAST_SCROLL_POSITION, position)
          return@doOnSaveState
        }
      }

      outState.remove<Nothing>(LAST_SCROLL_POSITION)
    }

    doOnInflate {
      val margin = 16.asDp(binding.watchlistList.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      LinearMarginDecoration.create(margin = margin).apply {
        binding.watchlistList.addItemDecoration(this)
      }

      // The bottom has additional space to fit the FAB
      val bottomMargin = 56.asDp(binding.watchlistList.context)
      LinearBoundsMarginDecoration(bottomMargin = bottomMargin).apply {
        binding.watchlistList.addItemDecoration(this)
      }
    }

    doOnInflate {
      FastScrollerBuilder(binding.watchlistList)
          .useMd2Style()
          .setPopupTextProvider(usingAdapter())
          .build()
    }

    doOnTeardown { binding.watchlistList.removeAllItemDecorations() }

    doOnTeardown {
      binding.watchlistList.adapter = null

      binding.watchlistSwipeRefresh.setOnRefreshListener(null)

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): QuoteAdapter {
    return requireNotNull(modelAdapter)
  }

  override fun onRefresh() {
    publish(WatchListViewEvent.ForceRefresh)
  }

  override fun onRemove(index: Int) {
    publish(WatchListViewEvent.Remove(index))
  }

  override fun onRender(state: UiRender<WatchListViewState>) {
    state.mapChanged { it.quotes }.render(viewScope) { handleList(it) }
    state.mapChanged { it.isLoading }.render(viewScope) { handleLoading(it) }
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomOffset(it) }
  }

  private fun handleBottomOffset(offset: Int) {
    layoutRoot.updatePadding(bottom = offset)
  }

  @CheckResult
  private fun createQuoteData(pair: QuotePair): QuoteViewState.QuoteData {
    return when {
      pair.quote != null -> QuoteViewState.QuoteData.Quote(requireNotNull(pair.quote))
      pair.error != null -> QuoteViewState.QuoteData.Error(requireNotNull(pair.error))
      else -> throw IllegalStateException("Missing quote and error for symbol ${pair.symbol}")
    }
  }

  private fun setList(list: List<QuotePair>) {
    val data = list.map { QuoteViewState(symbol = it.symbol, data = createQuoteData(it)) }
    Timber.d("Submit data list: $data")
    usingAdapter().submitList(data)
  }

  private fun clearList() {
    usingAdapter().submitList(null)
  }

  private fun handleLoading(loading: Boolean) {
    binding.watchlistSwipeRefresh.isRefreshing = loading
  }

  private fun handleList(schedule: List<QuotePair>) {
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
