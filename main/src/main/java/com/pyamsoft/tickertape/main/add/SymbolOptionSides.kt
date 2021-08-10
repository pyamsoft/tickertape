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
import com.google.android.material.tabs.TabLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.main.databinding.SymbolAddOptionSidesBinding
import com.pyamsoft.tickertape.stocks.api.HoldingType
import javax.inject.Inject
import timber.log.Timber

class SymbolOptionSides @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<SymbolAddViewState, SymbolAddViewEvent, SymbolAddOptionSidesBinding>(parent) {

  override val viewBinding = SymbolAddOptionSidesBinding::inflate

  override val layoutRoot by boundView { symbolAddOptionRoot }

  init {
    doOnInflate {
      addTabs()
      attachListener()
    }
  }

  private fun addTabs() {
    binding.symbolAddOptionSides.apply {
      addTab(newTab().setText(SIDE_BUY))
      addTab(newTab().setText(SIDE_SELL))
    }
  }

  private fun attachListener() {
    val listener =
        object : TabLayout.OnTabSelectedListener {

          override fun onTabSelected(tab: TabLayout.Tab) {
            return when (val page = tab.text) {
              SIDE_SELL, SIDE_BUY -> publish(SymbolAddViewEvent.UpdateOptionSide)
              else -> Timber.w("Tab selected but type invalid: $page")
            }
          }

          override fun onTabUnselected(tab: TabLayout.Tab) {}

          override fun onTabReselected(tab: TabLayout.Tab) {}
        }

    binding.symbolAddOptionSides.apply {
      addOnTabSelectedListener(listener)
      doOnTeardown { removeOnTabSelectedListener(listener) }
    }
  }

  override fun onRender(state: UiRender<SymbolAddViewState>) {
    state.mapChanged { it.type }.render(viewScope) { handlePage(it) }
  }

  private fun handlePage(pageType: AddPageType) {
    val type =
        when (pageType) {
          is AddPageType.Portfolio -> pageType.holdingType
          is AddPageType.Watchlist -> {
            Timber.w("Unable to handle option sides in watchlist page")
            return
          }
        }

    val tabs = binding.symbolAddOptionSides
    for (i in 0 until tabs.tabCount) {
      val tab = tabs.getTabAt(i)
      if (tab == null) {
        Timber.w("No tab found at index: $i")
        continue
      }

      val page =
          when (type) {
            is HoldingType.Options.Buy -> SIDE_BUY
            is HoldingType.Options.Sell -> SIDE_SELL
            else -> {
              Timber.w("Cannot handle page when type is not Options: $type")
              return
            }
          }

      val tag = tab.text
      if (tag == page) {
        tabs.selectTab(tab, true)
        break
      }
    }
  }

  companion object {

    private const val SIDE_BUY = "Buy"
    private const val SIDE_SELL = "Sell"
  }
}
