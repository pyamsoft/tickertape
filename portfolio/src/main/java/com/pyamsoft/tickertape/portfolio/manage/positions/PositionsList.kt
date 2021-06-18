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

package com.pyamsoft.tickertape.portfolio.manage.positions

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.databinding.PositionListBinding
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingViewEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingViewState
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemComponent
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemViewState
import io.cabriole.decorator.LinearBoundsMarginDecoration
import io.cabriole.decorator.LinearMarginDecoration
import javax.inject.Inject
import timber.log.Timber

class PositionsList
@Inject
internal constructor(
    parent: ViewGroup,
    owner: LifecycleOwner,
    factory: PositionItemComponent.Factory
) :
    BaseUiView<HoldingViewState, HoldingViewEvent, PositionListBinding>(parent),
    SwipeRefreshLayout.OnRefreshListener,
    PositionsAdapter.Callback {

  override val viewBinding = PositionListBinding::inflate

  override val layoutRoot by boundView { positionListRoot }

  private var modelAdapter: PositionsAdapter? = null

  private var lastScrollPosition = 0

  init {
    doOnInflate {
      binding.positionListList.layoutManager =
          LinearLayoutManager(binding.positionListList.context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 3
          }
    }

    doOnInflate {
      modelAdapter = PositionsAdapter.create(factory, owner, this)
      binding.positionListList.adapter = modelAdapter
    }

    doOnInflate { binding.positionListSwipeRefresh.setOnRefreshListener(this) }

    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = binding.positionListList.layoutManager
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
      val margin = 16.asDp(binding.positionListList.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      LinearMarginDecoration.create(margin = margin).apply {
        binding.positionListList.addItemDecoration(this)
      }

      // The bottom has additional space to fit the FAB
      val bottomMargin = 56.asDp(binding.positionListList.context)
      LinearBoundsMarginDecoration(bottomMargin = bottomMargin).apply {
        binding.positionListList.addItemDecoration(this)
      }
    }

    doOnTeardown { binding.positionListList.removeAllItemDecorations() }

    doOnTeardown {
      binding.positionListList.adapter = null

      binding.positionListSwipeRefresh.setOnRefreshListener(null)

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): PositionsAdapter {
    return requireNotNull(modelAdapter)
  }

  override fun onRefresh() {
    publish(HoldingViewEvent.ForceRefresh)
  }

  override fun onRemove(index: Int) {
    publish(HoldingViewEvent.Remove(index))
  }

  override fun onRender(state: UiRender<HoldingViewState>) {
    state.mapChanged { it.stock }.render(viewScope) { handleList(it) }
    state.mapChanged { it.isLoading }.render(viewScope) { handleLoading(it) }
  }

  private fun setList(holding: DbHolding, positions: List<DbPosition>) {
    val data = positions.map { PositionItemViewState(holding = holding, position = it) }
    Timber.d("Submit data list: $data")
    usingAdapter().submitList(data)
  }

  private fun clearList() {
    usingAdapter().submitList(null)
  }

  private fun handleLoading(loading: Boolean) {
    binding.positionListSwipeRefresh.isRefreshing = loading
  }

  private fun handleList(stock: PortfolioStock?) {
    if (stock == null) {
      clearList()
    } else {
      val positions = stock.positions
      if (positions.isEmpty()) {
        clearList()
      } else {
        setList(stock.holding, positions)
      }
    }
  }

  companion object {
    private const val LAST_SCROLL_POSITION = "portfolio_last_scroll_position"
  }
}
