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

import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.home.databinding.HomeGainersBinding
import com.pyamsoft.tickertape.home.index.HomeIndexComponent
import com.pyamsoft.tickertape.quote.QuotedChart
import javax.inject.Inject

class HomeGainerList
@Inject
internal constructor(
    parent: ViewGroup,
    factory: HomeIndexComponent.Factory,
    owner: LifecycleOwner,
) : BaseHomeChartList<HomeGainersBinding>(parent, factory, owner) {

  override val layoutRoot by boundView { homeGainersRoot }

  override val viewBinding = HomeGainersBinding::inflate

  override fun provideList(): RecyclerView {
    return binding.homeGainers
  }

  override fun provideTitle(): TextView {
    return binding.homeGainersTitle
  }

  override fun provideError(): TextView {
    return binding.homeGainersError
  }

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.gainers }.render(viewScope) { handleGainersChanged(it) }
    state.mapChanged { it.gainError }.render(viewScope) { handleError(it) }
  }

  private fun handleGainersChanged(gainers: List<TopDataWithChart>) {
    val list =
        gainers.map { QuotedChart(symbol = it.quote.symbol(), quote = it.quote, chart = it.chart) }
    handleList(list)

    val title = gainers.firstOrNull()?.title.orEmpty()
    handleTitle(title)
  }
}
