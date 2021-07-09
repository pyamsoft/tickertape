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

package com.pyamsoft.tickertape.main

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.CheckResult
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.util.doOnApplyWindowInsets
import com.pyamsoft.tickertape.main.databinding.MainBottomBarBinding
import javax.inject.Inject
import timber.log.Timber

class MainBar @Inject internal constructor(parent: ViewGroup, owner: LifecycleOwner) :
    BaseUiView<MainViewState, MainViewEvent, MainBottomBarBinding>(parent) {

  override val viewBinding = MainBottomBarBinding::inflate

  override val layoutRoot by boundView { mainBarRoot }

  private val handler = Handler(Looper.getMainLooper())

  init {

    doOnInflate { layoutRoot.outlineProvider = ViewOutlineProvider.BACKGROUND }

    doOnInflate { binding.mainBarNav.outlineProvider = null }

    doOnInflate { binding.mainBarNav.menu.findItem(R.id.menu_placeholder)?.isEnabled = false }

    doOnInflate {
      layoutRoot.doOnApplyWindowInsets(owner) { view, _, _ ->

        // Ensure this happens last
        view.post {
          // Set all padding to zero or bar is too puffy with nav buttons
          view.updatePadding(left = 0, right = 0, top = 0, bottom = 0)

          // Make sure we are laid out before grabbing the height
          view.post {
            // Publish the measured height
            publish(MainViewEvent.BottomBarMeasured(view.height))
          }
        }
      }
    }

    doOnInflate {
      binding.mainBarNav.setOnNavigationItemSelectedListener { item ->
        Timber.d("Click nav item: $item")
        return@setOnNavigationItemSelectedListener when (item.itemId) {
          R.id.menu_watchlist -> select(MainViewEvent.OpenWatchList)
          R.id.menu_settings -> select(MainViewEvent.OpenSettings)
          R.id.menu_portfolio -> select(MainViewEvent.OpenPortfolio)
          R.id.menu_home -> select(MainViewEvent.OpenHome)
          else -> false
        }
      }
    }

    doOnTeardown { binding.mainBarNav.setOnNavigationItemSelectedListener(null) }

    doOnTeardown { handler.removeCallbacksAndMessages(null) }
  }

  override fun onRender(state: UiRender<MainViewState>) {
    state.mapChanged { it.page }.render(viewScope) { handlePage(it) }
  }

  private fun handlePage(page: MainPage) {
    Timber.d("Handle page: $page")
    val pageId = getIdForPage(page)
    if (pageId != 0) {
      Timber.d("Mark page selected: $page $pageId")
      // Don't mark it selected since this will re-fire the click event
      // binding.mainBarNav.selectedItemId = pageId
      val item = binding.mainBarNav.menu.findItem(pageId)
      if (item != null) {
        handler.removeCallbacksAndMessages(null)
        handler.post { item.isChecked = true }
      }
    }
  }

  @CheckResult
  private fun select(viewEvent: MainViewEvent): Boolean {
    publish(viewEvent)
    return false
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun getIdForPage(page: MainPage): Int {
      return when (page) {
        is MainPage.Home -> R.id.menu_home
        is MainPage.WatchList -> R.id.menu_watchlist
        is MainPage.Settings -> R.id.menu_settings
        is MainPage.Portfolio -> R.id.menu_portfolio
      }
    }
  }
}
