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

package com.pyamsoft.tickertape.portfolio

import android.view.LayoutInflater
import androidx.annotation.CheckResult
import com.google.android.material.tabs.TabLayout
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioTabsBinding
import com.pyamsoft.tickertape.portfolio.manage.PortfolioPage
import javax.inject.Inject
import timber.log.Timber

class PortfolioTabs @Inject internal constructor(appBarActivity: AppBarActivity) :
    UiView<PortfolioViewState, PortfolioViewEvent>() {

  private var _bindingRoot: TabLayout? = null
  private val layoutRoot: TabLayout
    get() = requireNotNull(_bindingRoot)

  init {
    // Replace the app bar background during switcher presence
    doOnInflate {
      appBarActivity.requireAppBar { appBar ->
        val inflater = LayoutInflater.from(appBar.context)
        val binding = PortfolioTabsBinding.inflate(inflater, appBar)
        _bindingRoot = binding.portfolioTabs.also { onCreate(it) }
      }
    }

    doOnTeardown {
      _bindingRoot?.also { tabs ->
        appBarActivity.withAppBar { appBar -> appBar.removeView(tabs) }
        onDestroy(tabs)
      }
    }
  }

  private fun onCreate(tabs: TabLayout) {
    addTabs(tabs)
    attachListener(tabs)
  }

  private fun onDestroy(tabs: TabLayout) {
    tabs.removeAllTabs()
  }

  private fun addTabs(tabs: TabLayout) {
    tabs.apply {
      for (e in PortfolioSection.values()) {
        addTab(newTab().setText(e.display).setTag(e))
      }
    }
  }

  private fun attachListener(tabs: TabLayout) {
    val listener =
        object : TabLayout.OnTabSelectedListener {

          override fun onTabSelected(tab: TabLayout.Tab) {
            val section = getTabSection(tab) ?: return
            return when (section) {
              PortfolioSection.STOCKS -> publish(PortfolioViewEvent.ShowStocks)
              PortfolioSection.OPTIONS -> publish(PortfolioViewEvent.ShowOptions)
            }
          }

          override fun onTabUnselected(tab: TabLayout.Tab) {}

          override fun onTabReselected(tab: TabLayout.Tab) {}
        }

    tabs.apply {
      addOnTabSelectedListener(listener)
      doOnTeardown { removeOnTabSelectedListener(listener) }
    }
  }

  private fun handleSection(section: PortfolioSection) {
    val tabs = layoutRoot
    for (i in 0 until tabs.tabCount) {
      val tab = tabs.getTabAt(i)
      if (tab == null) {
        Timber.w("No tab found at index: $i")
        continue
      }

      val tag = getTabSection(tab)
      if (tag == section) {
        tabs.selectTab(tab, true)
        break
      }
    }
  }

  override fun render(state: UiRender<PortfolioViewState>) {
    state.mapChanged { it.section }.render(viewScope) { handleSection(it) }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun getTabSection(tab: TabLayout.Tab): PortfolioSection? {
      val tag = tab.tag
      if (tag == null) {
        Timber.w("No tag found on tab: $tab")
        return null
      }

      if (tag !is PortfolioSection) {
        Timber.w("Tag is not PortfolioSection model: $tag")
        return null
      }

      return tag
    }
  }
}
