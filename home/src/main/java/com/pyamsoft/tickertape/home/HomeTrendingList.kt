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
import com.pyamsoft.tickertape.home.databinding.HomeTrendingBinding
import com.pyamsoft.tickertape.home.index.HomeIndexComponent
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.ui.PackedData
import javax.inject.Inject

class HomeTrendingList
@Inject
internal constructor(
    parent: ViewGroup,
    factory: HomeIndexComponent.Factory,
    owner: LifecycleOwner,
    pool: RecyclerView.RecycledViewPool,
) : BaseHomeChartList<HomeTrendingBinding>(parent, factory, owner, pool) {

  override val layoutRoot by boundView { homeTrendingRoot }

  override val viewBinding = HomeTrendingBinding::inflate

  init {
    doOnInflate { binding.homeTrendingTitle.text = "Trending Tickers US" }

    doOnTeardown { binding.homeTrendingTitle.text = null }
  }

  override fun provideChartType(): HomeChartType {
    return HomeChartType.TRENDING
  }

  override fun provideList(): RecyclerView {
    return binding.homeTrending
  }

  override fun provideError(): TextView {
    return binding.homeTrendingError
  }

  override fun provideEmpty(): View {
    return binding.homeTrendingEmptyState
  }

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.trending }.render(viewScope) { handleTrendingChanged(it) }
  }

  private fun handleTrendingChanged(trending: PackedData<List<TopDataWithChart>>) {
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
