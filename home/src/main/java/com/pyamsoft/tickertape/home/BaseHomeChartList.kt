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
import androidx.annotation.CheckResult
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.ui.util.removeAllItemDecorations
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.tickertape.home.index.HomeIndexAdapter
import com.pyamsoft.tickertape.home.index.HomeIndexComponent
import com.pyamsoft.tickertape.home.index.HomeIndexViewState
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.ui.getUserMessage
import io.cabriole.decorator.DecorationLookup
import io.cabriole.decorator.LinearMarginDecoration
import timber.log.Timber

abstract class BaseHomeChartList<B : ViewBinding>
protected constructor(
    parent: ViewGroup,
    factory: HomeIndexComponent.Factory,
    owner: LifecycleOwner,
) : BaseUiView<HomeViewState, HomeViewEvent, B>(parent) {

  private var modelAdapter: HomeIndexAdapter? = null

  private var lastScrollPosition = 0

  init {
    doOnInflate {
      val list = provideList()
      list.layoutManager =
          LinearLayoutManager(list.context).apply {
            orientation = RecyclerView.HORIZONTAL
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 3
          }
    }

    doOnInflate {
      modelAdapter = HomeIndexAdapter.create(factory, owner)
      provideList().adapter = modelAdapter
    }

    doOnInflate { savedInstanceState ->
      val position = savedInstanceState.get(LAST_SCROLL_POSITION) ?: -1
      if (position >= 0) {
        Timber.d("Last scroll position saved at: $position")
        lastScrollPosition = position
      }
    }

    doOnSaveState { outState ->
      val manager = provideList().layoutManager
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
      val list = provideList()
      val margin = 16.asDp(list.context)

      // Standard margin on all items
      // For some reason, the margin registers only half as large as it needs to
      // be, so we must double it.
      //
      // First item is weird.
      LinearMarginDecoration(
              leftMargin = margin * 2,
              rightMargin = margin,
              orientation = RecyclerView.HORIZONTAL,
              decorationLookup =
                  object : DecorationLookup {
                    override fun shouldApplyDecoration(position: Int, itemCount: Int): Boolean {
                      return position == 0
                    }
                  })
          .apply { list.addItemDecoration(this) }

      LinearMarginDecoration.createHorizontal(
              margin,
              orientation = RecyclerView.HORIZONTAL,
              decorationLookup =
                  object : DecorationLookup {
                    override fun shouldApplyDecoration(position: Int, itemCount: Int): Boolean {
                      return position > 0
                    }
                  })
          .apply { list.addItemDecoration(this) }

      LinearMarginDecoration.createVertical(margin, orientation = RecyclerView.HORIZONTAL).apply {
        list.addItemDecoration(this)
      }
    }

    doOnTeardown { provideList().removeAllItemDecorations() }

    doOnTeardown {
      provideList().adapter = null
      provideTitle().text = null

      modelAdapter = null
    }
  }

  @CheckResult
  private fun usingAdapter(): HomeIndexAdapter {
    return requireNotNull(modelAdapter)
  }

  private fun setList(list: List<QuotedChart>) {
    val data =
        list.map { HomeIndexViewState(symbol = it.symbol, chart = it.chart, quote = it.quote) }
    usingAdapter().submitList(data)
  }

  private fun clearList() {
    usingAdapter().submitList(null)
  }

  protected fun handleList(list: List<QuotedChart>) {
    if (list.isEmpty()) {
      clearList()
    } else {
      setList(list)
    }
  }

  protected fun handleError(throwable: Throwable?) {
    val list = provideList()
    val error = provideError()
    if (throwable == null) {
      error.isGone = true
      list.isVisible = true
    } else {
      error.text = throwable.getUserMessage()
      error.isVisible = true
      list.isGone = true
    }
  }

  protected fun handleTitle(title: String) {
    provideTitle().text = title
  }

  // We must provide the list here and use a unique ViewBinding, even though
  // the layout files are all the same. We need different layout files with different view IDs
  // because of the way view_binding works when adding multiple views with the same ID to the same
  // parent view. Since viewBinding internally calls findViewById, the find call will always resolve
  // to the "first" child, even if it is not actually contained within the current binding.
  @CheckResult protected abstract fun provideList(): RecyclerView

  // Same goes for the title text view
  @CheckResult protected abstract fun provideTitle(): TextView

  // Same goes for the error text view
  @CheckResult protected abstract fun provideError(): TextView

  companion object {

    private const val LAST_SCROLL_POSITION = "last_scroll_position"
  }
}
