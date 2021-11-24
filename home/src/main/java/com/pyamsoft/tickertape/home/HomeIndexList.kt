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
import com.pyamsoft.tickertape.home.databinding.HomeIndexesBinding
import com.pyamsoft.tickertape.home.index.HomeIndexComponent
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.ui.PackedData
import javax.inject.Inject

class HomeIndexList
@Inject
internal constructor(
    parent: ViewGroup,
    factory: HomeIndexComponent.Factory,
    owner: LifecycleOwner,
    pool: RecyclerView.RecycledViewPool,
) : BaseHomeChartList<HomeIndexesBinding>(parent, factory, owner, pool) {

  override val layoutRoot by boundView { homeIndexesRoot }

  override val viewBinding = HomeIndexesBinding::inflate

  init {
    doOnInflate { binding.homeIndexesTitle.text = "Market Indexes" }

    doOnTeardown { binding.homeIndexesTitle.text = null }
  }

  override fun provideChartType(): HomeChartType {
    return HomeChartType.INDEX
  }

  override fun provideList(): RecyclerView {
    return binding.homeIndexes
  }

  override fun provideEmpty(): View {
    return binding.homeIndexesEmptyState
  }

  override fun provideError(): TextView {
    return binding.homeIndexesError
  }

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.indexes }.render(viewScope) { handleIndexesChanged(it) }
  }

  private fun handleIndexesChanged(indexes: PackedData<List<Ticker>>) {
    return when (indexes) {
      is PackedData.Data -> {
        handleList(indexes.value)
      }
      is PackedData.Error -> handleError(indexes.throwable)
    }
  }
}
