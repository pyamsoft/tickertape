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

import com.google.android.material.tabs.TabLayout
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.tickertape.ui.UiTabs
import javax.inject.Inject

class PortfolioTabs @Inject internal constructor(appBarActivity: AppBarActivity) :
    UiTabs<PortfolioViewState, PortfolioViewEvent, PortfolioTabSection>(appBarActivity) {

  override fun addTabs(tabs: TabLayout) {
    tabs.apply {
      for (e in PortfolioTabSection.values()) {
        addTab(newTab().setText(e.display).setTag(e))
      }
    }
  }

  override fun handleTabSelected(tab: TabLayout.Tab) {
    val section = getTabSection(tab, PortfolioTabSection::class.java) ?: return
    return when (section) {
      PortfolioTabSection.STOCK -> publish(PortfolioViewEvent.ShowStocks)
      PortfolioTabSection.OPTION -> publish(PortfolioViewEvent.ShowOptions)
      PortfolioTabSection.CRYPTO -> publish(PortfolioViewEvent.ShowCrypto)
    }
  }

  override fun render(state: UiRender<PortfolioViewState>) {
    state.mapChanged { it.section }.render(viewScope) { handleSection(it) }
  }
}
