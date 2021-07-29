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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioListBinding
import com.pyamsoft.tickertape.portfolio.item.PortfolioAdapter
import com.pyamsoft.tickertape.portfolio.item.PortfolioItemComponent
import com.pyamsoft.tickertape.portfolio.item.PortfolioItemViewState
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.getUserMessage
import io.cabriole.decorator.LinearBoundsMarginDecoration
import io.cabriole.decorator.LinearMarginDecoration
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import timber.log.Timber

abstract class BasePortfolioList
protected constructor(
    parent: ViewGroup,
    owner: LifecycleOwner,
    appBarActivity: AppBarActivity,
    factory: PortfolioItemComponent.Factory
) :
    BaseUiView<PortfolioViewState, PortfolioViewEvent, PortfolioListBinding>(parent),
    SwipeRefreshLayout.OnRefreshListener,
    PortfolioAdapter.Callback {

  final override val viewBinding = PortfolioListBinding::inflate

  final override val layoutRoot by boundView { portfolioListSwipeRefresh }

  private var modelAdapter: PortfolioAdapter? = null

  private var bottomDecoration: RecyclerView.ItemDecoration? = null
  private var lastScrollPosition = 0

  protected abstract val includeHeader: Boolean

  init {
    doOnInflate {
      binding.portfolioListList.layoutManager =
          LinearLayoutManager(binding.portfolioListList.context).apply {
        isItemPrefetchEnabled = true
        initialPrefetchItemCount = 3
      }
    }

    doOnInflate {
      modelAdapter = PortfolioAdapter.create(factory, owner, appBarActivity, this)
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
      LinearMarginDecoration.create(margin).apply {
        binding.portfolioListList.addItemDecoration(this)
      }
    }

    doOnInflate {
      FastScrollerBuilder(binding.portfolioListList)
          .useMd2Style()
          .setPopupTextProvider(usingAdapter())
          .build()
    }

    doOnTeardown {
      binding.portfolioListList.removeAllItemDecorations()
      bottomDecoration = null
    }

    doOnTeardown {
      binding.portfolioListList.adapter = null

      binding.portfolioListSwipeRefresh.setOnRefreshListener(null)
      binding.portfolioListError.text = null

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): PortfolioAdapter {
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
    state.render(viewScope) { handlePortfolio(it) }
    state.mapChanged { it.isLoading }.render(viewScope) { handleLoading(it) }
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomOffset(it) }
  }

  private fun handlePortfolio(state: PortfolioViewState) {
    return when (val portfolio = state.portfolio) {
      is PackedData.Data -> handleList(portfolio.value, state)
      is PackedData.Error -> handleError(portfolio.throwable)
    }
  }

  private fun handleError(throwable: Throwable) {
    binding.apply {
      portfolioListList.isGone = true
      portfolioListEmptyState.isGone = true
      portfolioListError.isVisible = true

      portfolioListError.text = throwable.getUserMessage()
    }
  }

  private fun handleBottomOffset(height: Int) {
    // Add additional padding to the list bottom to account for the height change in MainContainer
    bottomDecoration?.also { binding.portfolioListList.removeItemDecoration(it) }
    bottomDecoration =
        LinearBoundsMarginDecoration(bottomMargin = (height * 1.5).toInt()).apply {
      binding.portfolioListList.addItemDecoration(this)
    }
  }

  private fun setList(items: List<PortfolioStock>, state: PortfolioViewState) {
    val data = items.map { PortfolioItemViewState.Item(stock = it) }
    Timber.d("Submit data list: $data")
    val list = if (includeHeader) listOf(PortfolioItemViewState.Header(state)) + data else data
    usingAdapter().submitList(listOf(PortfolioItemViewState.Spacer) + list)

    binding.apply {
      portfolioListError.isGone = true
      portfolioListEmptyState.isGone = true
      portfolioListList.isVisible = true
    }
  }

  private fun clearList() {
    usingAdapter().submitList(null)

    binding.apply {
      portfolioListError.isGone = true
      portfolioListList.isGone = true
      portfolioListEmptyState.isVisible = true
    }
  }

  private fun handleLoading(loading: Boolean) {
    binding.portfolioListSwipeRefresh.isRefreshing = loading
  }

  private fun handleList(list: PortfolioStockList, state: PortfolioViewState) {
    val portfolio = list.list
    if (portfolio.isEmpty()) {
      clearList()
    } else {
      setList(portfolio, state)
    }
  }

  companion object {
    private const val LAST_SCROLL_POSITION = "portfolio_last_scroll_position"
  }
}
