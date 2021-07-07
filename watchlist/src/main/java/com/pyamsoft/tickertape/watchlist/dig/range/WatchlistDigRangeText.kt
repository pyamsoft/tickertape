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

package com.pyamsoft.tickertape.watchlist.dig.range

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.ui.R
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistDigRangeItemTextBinding
import java.util.Locale
import javax.inject.Inject

class WatchlistDigRangeText @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<
        WatchlistDigRangeViewState, WatchlistDigRangeViewEvent, WatchlistDigRangeItemTextBinding>(
        parent) {

  override val layoutRoot by boundView { watchlistDigRangeItemText }

  override val viewBinding = WatchlistDigRangeItemTextBinding::inflate

  init {
    doOnTeardown { binding.watchlistDigRangeItemText.text = "" }
  }

  override fun onRender(state: UiRender<WatchlistDigRangeViewState>) {
    state.mapChanged { it.range }.render(viewScope) { handleRangeChanged(it) }
    state.mapChanged { it.isSelected }.render(viewScope) { handleSelectedChanged(it) }
  }

  private fun handleSelectedChanged(selected: Boolean) {
    binding.watchlistDigRangeItemText.setBackgroundResource(if (selected) R.color.blue500 else 0)
  }

  private fun handleRangeChanged(range: StockChart.IntervalRange) {
    binding.watchlistDigRangeItemText.text = range.apiValue.uppercase(Locale.getDefault())
  }
}
