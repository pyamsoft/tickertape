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

package com.pyamsoft.tickertape.quote.ui.component.chart

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.quote.ui.component.chart.range.ChartRangeAdapter
import com.pyamsoft.tickertape.quote.ui.component.chart.range.ChartRangeComponent
import com.pyamsoft.tickertape.quote.ui.component.chart.range.ChartRangeViewState
import com.pyamsoft.tickertape.quote.ui.databinding.ComponentChartRangesBinding
import com.pyamsoft.tickertape.stocks.api.StockChart
import io.cabriole.decorator.DecorationLookup
import io.cabriole.decorator.LinearMarginDecoration
import javax.inject.Inject
import timber.log.Timber

internal class StockChartRanges
@Inject
internal constructor(
    factory: ChartRangeComponent.Factory,
    owner: LifecycleOwner,
    parent: ViewGroup
) :
    BaseUiView<StockChartViewState, StockChartViewEvent, ComponentChartRangesBinding>(parent),
    ChartRangeAdapter.Callback {

  override val layoutRoot by boundView { componentChartRanges }

  override val viewBinding = ComponentChartRangesBinding::inflate

  private var modelAdapter: ChartRangeAdapter? = null

  private var lastScrollPosition = 0

  init {
    doOnInflate {
      binding.componentChartRanges.layoutManager =
          LinearLayoutManager(binding.componentChartRanges.context).apply {
            orientation = RecyclerView.HORIZONTAL
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 3
          }
    }

    doOnInflate {
      modelAdapter = ChartRangeAdapter.create(factory, owner, this)
      binding.componentChartRanges.adapter = modelAdapter
    }
    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = binding.componentChartRanges.layoutManager
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
      val margin = 16.asDp(layoutRoot.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      //
      // First item is weird.
      LinearMarginDecoration(
              rightMargin = margin,
              orientation = RecyclerView.HORIZONTAL,
              decorationLookup =
                  object : DecorationLookup {
                    override fun shouldApplyDecoration(position: Int, itemCount: Int): Boolean {
                      return position == 0
                    }
                  })
          .apply { binding.componentChartRanges.addItemDecoration(this) }

      LinearMarginDecoration.createHorizontal(
              margin,
              orientation = RecyclerView.HORIZONTAL,
              decorationLookup =
                  object : DecorationLookup {
                    override fun shouldApplyDecoration(position: Int, itemCount: Int): Boolean {
                      return position > 0 && position < itemCount - 1
                    }
                  })
          .apply { binding.componentChartRanges.addItemDecoration(this) }
    }

    doOnTeardown { binding.componentChartRanges.removeAllItemDecorations() }

    doOnTeardown {
      binding.componentChartRanges.adapter = null

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): ChartRangeAdapter {
    return requireNotNull(modelAdapter)
  }

  override fun onSelect(index: Int) {
    publish(StockChartViewEvent.RangeUpdated(index))
  }

  override fun onRender(state: UiRender<StockChartViewState>) {
    state.render(viewScope) { handleList(it) }
  }

  private fun setList(current: StockChart.IntervalRange, ranges: List<StockChart.IntervalRange>) {
    val data = ranges.map { ChartRangeViewState(isSelected = it == current, range = it) }
    Timber.d("Submit data list: $data")
    usingAdapter().submitList(data)
  }

  private fun clearList() {
    usingAdapter().submitList(null)
  }

  private fun handleList(state: StockChartViewState) {
    val current = state.currentRange
    val ranges = state.ranges

    if (ranges.isEmpty()) {
      clearList()
    } else {
      setList(current, ranges)
    }
  }

  companion object {

    private const val LAST_SCROLL_POSITION = "last_scroll_position"
  }
}
