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

package com.pyamsoft.tickertape.portfolio.manage

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.google.android.material.tabs.TabLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.ImageTarget
import com.pyamsoft.pydroid.loader.Loaded
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.util.tintWith
import com.pyamsoft.tickertape.portfolio.R as R2
import com.pyamsoft.tickertape.portfolio.databinding.ManagePortfolioToolbarBinding
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.withRoundedBackground
import javax.inject.Inject
import timber.log.Timber

class ManagePortfolioToolbar
@Inject
internal constructor(
    private val imageLoader: ImageLoader,
    parent: ViewGroup,
) :
    BaseUiView<ManagePortfolioViewState, ManagePortfolioViewEvent, ManagePortfolioToolbarBinding>(
        parent) {

  override val viewBinding = ManagePortfolioToolbarBinding::inflate

  override val layoutRoot by boundView { positionAppbar }

  private var customImageLoaded: Loaded? = null

  init {
    doOnInflate { binding.positionAppbar.withRoundedBackground() }

    doOnInflate {
      binding.positionToolbar.setNavigationOnClickListener(
          DebouncedOnClickListener.create { publish(ManagePortfolioViewEvent.Close) })
    }

    doOnTeardown { binding.positionToolbar.setNavigationOnClickListener(null) }

    doOnTeardown { unloadImage() }

    doOnTeardown { binding.positionToolbar.title = "" }

    doOnInflate { binding.positionToolbar.inflateMenu(R2.menu.manage_toolbar) }

    doOnInflate {
      binding.positionToolbar.setOnMenuItemClickListener { item ->
        when (item.itemId) {
          R2.id.menu_manage_toolbar_add -> publish(ManagePortfolioViewEvent.Add)
        }
        return@setOnMenuItemClickListener true
      }
    }

    doOnTeardown { binding.positionToolbar.setOnMenuItemClickListener(null) }

    doOnInflate {
      addTabs()
      attachListener()
    }
  }

  private fun addTabs() {
    binding.positionSwitcher.apply {
      for (e in PortfolioPage.values()) {
        addTab(newTab().setText(e.display).setTag(e))
      }
    }
  }

  private fun attachListener() {
    val listener =
        object : TabLayout.OnTabSelectedListener {

          override fun onTabSelected(tab: TabLayout.Tab) {
            val page = getTabPage(tab) ?: return
            return when (page) {
              PortfolioPage.POSITIONS -> publish(ManagePortfolioViewEvent.OpenPositions)
              PortfolioPage.QUOTE -> publish(ManagePortfolioViewEvent.OpenQuote)
            }
          }

          override fun onTabUnselected(tab: TabLayout.Tab) {}

          override fun onTabReselected(tab: TabLayout.Tab) {}
        }

    binding.positionSwitcher.apply {
      addOnTabSelectedListener(listener)
      doOnTeardown { removeOnTabSelectedListener(listener) }
    }
  }

  private fun loadDefaultImage() {
    unloadImage()
    binding.positionToolbar.setUpEnabled(true)
  }

  private fun loadCustomImage() {
    unloadImage()
    customImageLoaded =
        imageLoader
            .asDrawable()
            .load(R.drawable.ic_close_24dp)
            .mutate { it.tintWith(Color.WHITE) }
            .into(
                object : ImageTarget<Drawable> {

                  override fun clear() {
                    binding.positionToolbar.navigationIcon = null
                  }

                  override fun setImage(image: Drawable) {
                    binding.positionToolbar.setUpEnabled(true, image)
                  }
                })
  }

  private fun unloadImage() {
    customImageLoaded?.dispose()
    customImageLoaded = null
  }

  override fun onRender(state: UiRender<ManagePortfolioViewState>) {
    state.mapChanged { it.page }.render(viewScope) { handlePage(it) }
    state.mapChanged { it.page }.render(viewScope) { handleCloseState(it) }
    state.mapChanged { it.page }.render(viewScope) { handleMenuState(it) }
    state.mapChanged { it.symbol }.render(viewScope) { handleSymbol(it) }
  }

  private fun handlePage(page: PortfolioPage) {
    val tabs = binding.positionSwitcher
    for (i in 0 until tabs.tabCount) {
      val tab = tabs.getTabAt(i)
      if (tab == null) {
        Timber.w("No tab found at index: $i")
        continue
      }

      val tag = getTabPage(tab)
      if (tag == page) {
        tabs.selectTab(tab, true)
        break
      }
    }
  }

  private fun handleMenuState(page: PortfolioPage) {
    val isAddEnabled =
        when (page) {
          PortfolioPage.POSITIONS -> true
          PortfolioPage.QUOTE -> false
        }

    binding.positionToolbar.menu.findItem(R2.id.menu_manage_toolbar_add)?.isEnabled = isAddEnabled
  }

  private fun handleSymbol(symbol: StockSymbol) {
    binding.positionToolbar.title = symbol.symbol()
  }

  private fun handleCloseState(page: PortfolioPage) {
    val isClose =
        when (page) {
          PortfolioPage.POSITIONS, PortfolioPage.QUOTE -> true
        }

    if (isClose) {
      loadCustomImage()
    } else {
      loadDefaultImage()
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun getTabPage(tab: TabLayout.Tab): PortfolioPage? {
      val tag = tab.tag
      if (tag == null) {
        Timber.w("No tag found on tab: $tab")
        return null
      }

      if (tag !is PortfolioPage) {
        Timber.w("Tag is not PortfolioPage model: $tag")
        return null
      }

      return tag
    }
  }
}
