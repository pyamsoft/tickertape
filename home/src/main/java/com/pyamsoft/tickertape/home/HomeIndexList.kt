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
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.tickertape.home.databinding.HomeIndexesBinding
import com.pyamsoft.tickertape.home.index.HomeIndexAdapter
import com.pyamsoft.tickertape.home.index.HomeIndexComponent
import com.pyamsoft.tickertape.home.index.HomeIndexViewState
import com.pyamsoft.tickertape.quote.QuotedChart
import javax.inject.Inject
import timber.log.Timber

class HomeIndexList
@Inject
internal constructor(
    parent: ViewGroup,
    factory: HomeIndexComponent.Factory,
    owner: LifecycleOwner,
) : BaseUiView<HomeViewState, HomeViewEvent, HomeIndexesBinding>(parent) {

  override val layoutRoot by boundView { homeIndexes }

  override val viewBinding = HomeIndexesBinding::inflate

  private var modelAdapter: HomeIndexAdapter? = null

  private var lastScrollPosition = 0

  init {
    doOnInflate {
      binding.homeIndexes.layoutManager =
          LinearLayoutManager(binding.homeIndexes.context).apply {
        orientation = RecyclerView.HORIZONTAL
        isItemPrefetchEnabled = true
        initialPrefetchItemCount = 3
      }
    }

    doOnInflate {
      modelAdapter = HomeIndexAdapter.create(factory, owner)
      binding.homeIndexes.adapter = modelAdapter
    }

    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = binding.homeIndexes.layoutManager
      if (manager is LinearLayoutManager) {
        val position = manager.findFirstVisibleItemPosition()
        if (position > 0) {
          outState.put(LAST_SCROLL_POSITION, position)
          return@doOnSaveState
        }
      }

      outState.remove<Nothing>(LAST_SCROLL_POSITION)
    }

    doOnTeardown { binding.homeIndexes.removeAllItemDecorations() }

    doOnTeardown {
      binding.homeIndexes.adapter = null

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): HomeIndexAdapter {
    return requireNotNull(modelAdapter)
  }
  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.indexes }.render(viewScope) { handleList(it) }
  }

  private fun setList(indexes: List<QuotedChart>) {
    val data =
        indexes.map { HomeIndexViewState(symbol = it.symbol, chart = it.chart, quote = it.quote) }
    Timber.d("Submit data list: $data")
    usingAdapter().submitList(data)
  }

  private fun clearList() {
    usingAdapter().submitList(null)
  }

  private fun handleList(indexes: List<QuotedChart>) {
    if (indexes.isEmpty()) {
      clearList()
    } else {
      setList(indexes)
    }
  }

  companion object {

    private const val LAST_SCROLL_POSITION = "last_scroll_position"
  }
}
