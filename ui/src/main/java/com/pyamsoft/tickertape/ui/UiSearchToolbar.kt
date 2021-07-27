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

package com.pyamsoft.tickertape.ui

import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.iterator
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.app.ToolbarActivity

abstract class UiSearchToolbar<S : UiViewState, V : UiViewEvent>
protected constructor(private val toolbarActivity: ToolbarActivity) : UiView<S, V>() {

  private val publishHandler = Handler(Looper.getMainLooper())

  // NOTE(Peter): Hack because Android does not allow us to use Controlled view components like
  // React does by binding input and drawing to the render loop.
  //
  // This initialRenderPerformed variable allows us to set the initial state of a view once, and
  // bind listeners to
  // it because the state.item is only available in render instead of inflate. Once the firstRender
  // has set the view component up, the actual input will no longer be tracked via state render
  // events,
  // so the input is uncontrolled.
  private var initialRenderPerformed = false

  init {
    doOnInflate {
      toolbarActivity.withToolbar { toolbar ->
        toolbar.inflateMenu(R.menu.search)
        toolbar.menu.findItem(R.id.menu_search).requireNotNull().apply {
          setOnActionExpandListener(
              object : MenuItem.OnActionExpandListener {

                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                  toolbar.post { setVisibilityOfNonSearchItems(toolbar, false) }
                  return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                  toolbar.post { setVisibilityOfNonSearchItems(toolbar, true) }
                  return true
                }
              })

          actionView =
              SearchView(toolbar.context).apply {
            setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                  override fun onQueryTextSubmit(query: String): Boolean {
                    publishSearch(query)
                    return true
                  }

                  override fun onQueryTextChange(newText: String): Boolean {
                    publishSearch(newText)
                    return true
                  }
                })
          }
        }
      }
    }

    doOnTeardown {
      toolbarActivity.withToolbar { toolbar ->
        toolbar.handler?.removeCallbacksAndMessages(null)
        publishHandler.removeCallbacksAndMessages(null)
        setVisibilityOfNonSearchItems(toolbar, true)
        toolbar.menu.removeItem(R.id.menu_search)
      }
    }
  }

  private fun publishSearch(query: String) {
    publishHandler.removeCallbacksAndMessages(null)
    publishHandler.postDelayed({ publishSearchEvent(query) }, SEARCH_PUBLISH_TIMEOUT)
  }

  private fun setVisibilityOfNonSearchItems(toolbar: Toolbar, visible: Boolean) {
    for (item in toolbar.menu) {
      if (item.itemId != R.id.menu_search) {
        item.isVisible = visible
      }
    }
  }

  protected fun handleInitialSearch(search: String) {
    if (initialRenderPerformed) {
      return
    }
    initialRenderPerformed = true

    toolbarActivity.withToolbar { toolbar ->
      val item = toolbar.menu.findItem(R.id.menu_search) ?: return@withToolbar
      val searchView = item.actionView as? SearchView ?: return@withToolbar

      if (search.isNotBlank()) {
        if (item.expandActionView()) {
          searchView.setQuery(search, true)
        }
      }
    }
  }

  final override fun onFinalTeardown() {}

  final override fun onInit(savedInstanceState: UiSavedStateReader) {}

  protected abstract fun publishSearchEvent(search: String)

  companion object {
    private const val SEARCH_PUBLISH_TIMEOUT = 400L
  }
}
