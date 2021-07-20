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

package com.pyamsoft.tickertape.main.add

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.main.add.result.SearchResultAdapter
import com.pyamsoft.tickertape.main.add.result.SearchResultComponent
import com.pyamsoft.tickertape.main.add.result.SearchResultViewState
import com.pyamsoft.tickertape.main.databinding.SymbolAddResultListBinding
import com.pyamsoft.tickertape.stocks.api.SearchResult
import io.cabriole.decorator.LinearMarginDecoration
import javax.inject.Inject
import timber.log.Timber

class SymbolResultList
@Inject
internal constructor(
    factory: SearchResultComponent.Factory,
    owner: LifecycleOwner,
    parent: ViewGroup,
) :
    BaseUiView<SymbolAddViewState, SymbolAddViewEvent, SymbolAddResultListBinding>(parent),
    SearchResultAdapter.Callback {

  override val viewBinding = SymbolAddResultListBinding::inflate

  override val layoutRoot by boundView { symbolAddResultRoot }

  private var modelAdapter: SearchResultAdapter? = null

  private var lastScrollPosition = 0

  init {
    doOnInflate {
      binding.symbolAddResultList.layoutManager =
          LinearLayoutManager(binding.symbolAddResultList.context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 3
          }
    }

    doOnInflate {
      modelAdapter = SearchResultAdapter.create(factory, owner, this)
      binding.symbolAddResultList.adapter = modelAdapter
    }

    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = binding.symbolAddResultList.layoutManager
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
      val margin = 16.asDp(binding.symbolAddResultList.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      LinearMarginDecoration.create(margin = margin, orientation = RecyclerView.VERTICAL).apply {
        binding.symbolAddResultList.addItemDecoration(this)
      }
    }

    doOnTeardown { binding.symbolAddResultList.removeAllItemDecorations() }

    doOnTeardown {
      binding.symbolAddResultList.adapter = null

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): SearchResultAdapter {
    return requireNotNull(modelAdapter)
  }

  override fun onSelect(index: Int) {
    publish(SymbolAddViewEvent.SelectResult(index))
  }

  override fun onRender(state: UiRender<SymbolAddViewState>) {
    state.mapChanged { it.searchResults }.render(viewScope) { handleList(it) }
    state.mapChanged { it.searching }.render(viewScope) { handleSearching(it) }
  }

  private fun handleSearching(searching: Boolean) {
    if (searching) {
      binding.apply {
        symbolAddResultList.isGone = true
        symbolAddResultEmpty.isVisible = true
      }
    } else {
      binding.apply {
        symbolAddResultEmpty.isGone = true
        symbolAddResultList.isVisible = true
      }
    }
  }

  private fun clearList() {
    usingAdapter().submitList(null)
  }

  private fun setList(results: List<SearchResult>) {
    val list = results.map { SearchResultViewState(result = it) }
    usingAdapter().submitList(list)
  }

  private fun handleList(list: List<SearchResult>) {
    if (list.isEmpty()) {
      clearList()
    } else {
      setList(list)
    }
  }

  companion object {
    private const val LAST_SCROLL_POSITION = "portfolio_last_scroll_position"
  }
}
