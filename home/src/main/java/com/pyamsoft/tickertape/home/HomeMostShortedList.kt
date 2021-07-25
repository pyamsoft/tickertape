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

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.home.databinding.HomeMostShortedBinding
import com.pyamsoft.tickertape.home.index.HomeIndexComponent
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.ui.PackedData
import javax.inject.Inject

class HomeMostShortedList
@Inject
internal constructor(
    parent: ViewGroup,
    factory: HomeIndexComponent.Factory,
    owner: LifecycleOwner,
) : BaseHomeChartList<HomeMostShortedBinding>(parent, factory, owner) {

  override val layoutRoot by boundView { homeMostShortedRoot }

  override val viewBinding = HomeMostShortedBinding::inflate

  init {
    doOnInflate { binding.homeMostShortedTitle.text = "Most Shorted Stocks" }

    doOnTeardown { binding.homeMostShortedTitle.text = null }
  }

  override fun provideList(): RecyclerView {
    return binding.homeMostShorted
  }

  override fun provideError(): TextView {
    return binding.homeMostShortedError
  }

  override fun provideEmpty(): View {
    return binding.homeMostShortedEmptyState
  }

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.trending }.render(viewScope) { handleMostShortedChanged(it) }
  }

  private fun handleMostShortedChanged(trending: PackedData<List<TopDataWithChart>>) {
    return when (trending) {
      is PackedData.Data -> {
        val list =
            trending.value.map {
              QuotedChart(symbol = it.quote.symbol(), quote = it.quote, chart = it.chart)
            }
        handleList(list)
      }
      is PackedData.Error -> handleError(trending.throwable)
    }
  }
}
