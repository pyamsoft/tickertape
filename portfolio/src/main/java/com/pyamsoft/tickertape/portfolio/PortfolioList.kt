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

package com.pyamsoft.tickertape.portfolio

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioListBinding
import com.pyamsoft.tickertape.quote.QuoteAdapter
import com.pyamsoft.tickertape.quote.QuoteComponent
import com.pyamsoft.tickertape.quote.QuoteViewState
import com.pyamsoft.tickertape.quote.QuotedStock
import io.cabriole.decorator.LinearBoundsMarginDecoration
import io.cabriole.decorator.LinearMarginDecoration
import javax.inject.Inject
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import timber.log.Timber

class PortfolioList
@Inject
internal constructor(parent: ViewGroup, owner: LifecycleOwner, factory: QuoteComponent.Factory) :
    BaseUiView<PortfolioViewState, PortfolioViewEvent, PortfolioListBinding>(parent),
    SwipeRefreshLayout.OnRefreshListener,
    QuoteAdapter.Callback {

  override val viewBinding = PortfolioListBinding::inflate

  override val layoutRoot by boundView { portfolioListRoot }

  private var modelAdapter: QuoteAdapter? = null

  private var lastScrollPosition = 0

  init {
    doOnInflate {
      binding.portfolioListList.layoutManager =
          LinearLayoutManager(binding.portfolioListList.context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 3
          }
    }

    doOnInflate {
      modelAdapter = QuoteAdapter.create(factory, owner, this)
      binding.portfolioListList.adapter = modelAdapter
    }

    doOnInflate { binding.portfolioListSwipeRefresh.setOnRefreshListener(this) }

    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = binding.portfolioListList.layoutManager
      if (manager is LinearLayoutManager) {
        val position = manager.findFirstVisibleItemPosition()
        if (position > 0) {
          outState.put(LAST_SCROLL_POSITION, position)
          return@doOnSaveState
        }
      }

      outState.remove<Nothing>(LAST_SCROLL_POSITION)
    }

    doOnInflate {
      val margin = 16.asDp(binding.portfolioListList.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      LinearMarginDecoration.create(margin = margin).apply {
        binding.portfolioListList.addItemDecoration(this)
      }

      // The bottom has additional space to fit the FAB
      val bottomMargin = 56.asDp(binding.portfolioListList.context)
      LinearBoundsMarginDecoration(bottomMargin = bottomMargin).apply {
        binding.portfolioListList.addItemDecoration(this)
      }
    }

    doOnInflate {
      FastScrollerBuilder(binding.portfolioListList)
          .useMd2Style()
          .setPopupTextProvider(usingAdapter())
          .build()
    }

    doOnTeardown { binding.portfolioListList.removeAllItemDecorations() }

    doOnTeardown {
      binding.portfolioListList.adapter = null

      binding.portfolioListSwipeRefresh.setOnRefreshListener(null)

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): QuoteAdapter {
    return requireNotNull(modelAdapter)
  }

  override fun onSelect(index: Int) {
    publish(PortfolioViewEvent.Manage(index))
  }

  override fun onRefresh() {
    publish(PortfolioViewEvent.ForceRefresh)
  }

  override fun onRemove(index: Int) {
    publish(PortfolioViewEvent.Remove(index))
  }

  override fun onRender(state: UiRender<PortfolioViewState>) {
    state.mapChanged { it.portfolio }.render(viewScope) { handleList(it) }
    state.mapChanged { it.isLoading }.render(viewScope) { handleLoading(it) }
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomOffset(it) }
  }

  private fun handleBottomOffset(offset: Int) {
    layoutRoot.updatePadding(bottom = offset)
  }

  private fun setList(list: List<QuotedStock>) {
    val data = list.map { QuoteViewState(symbol = it.symbol, quote = it.quote) }
    Timber.d("Submit data list: $data")
    usingAdapter().submitList(data)
  }

  private fun clearList() {
    usingAdapter().submitList(null)
  }

  private fun handleLoading(loading: Boolean) {
    binding.portfolioListSwipeRefresh.isRefreshing = loading
  }

  private fun handleList(schedule: List<PortfolioStock>) {
    if (schedule.isEmpty()) {
      clearList()
    } else {
      // TODO don't map this since we want a different UI
      setList(schedule.mapNotNull { it.quote })
    }
  }

  companion object {
    private const val LAST_SCROLL_POSITION = "portfolio_last_scroll_position"
  }
}