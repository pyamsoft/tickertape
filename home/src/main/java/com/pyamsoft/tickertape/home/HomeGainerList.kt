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
import com.pyamsoft.tickertape.home.databinding.HomeGainersBinding
import com.pyamsoft.tickertape.home.index.HomeIndexComponent
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.ui.PackedData
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

  init {
    doOnInflate { binding.homeGainersTitle.text = "Day Gainers" }

    doOnTeardown { binding.homeGainersTitle.text = null }
  }

  override fun provideList(): RecyclerView {
    return binding.homeGainers
  }

  override fun provideEmpty(): View {
    return binding.homeGainersEmptyState
  }

  override fun provideError(): TextView {
    return binding.homeGainersError
  }

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.gainers }.render(viewScope) { handleGainersChanged(it) }
  }

  private fun handleGainersChanged(gainers: PackedData<List<TopDataWithChart>>) {
    return when (gainers) {
      is PackedData.Data -> {
        val list =
            gainers.value.map {
              QuotedChart(symbol = it.quote.symbol(), quote = it.quote, chart = it.chart)
            }
        handleList(list)
      }
      is PackedData.Error -> handleError(gainers.throwable)
    }
  }
}
