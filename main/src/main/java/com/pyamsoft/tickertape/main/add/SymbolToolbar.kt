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
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.tickertape.main.R
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.ui.UiDialogToolbar
import javax.inject.Inject

class SymbolToolbar
@Inject
internal constructor(
    imageLoader: ImageLoader,
    parent: ViewGroup,
) : UiDialogToolbar<SymbolAddViewState, SymbolAddViewEvent>(imageLoader, parent) {

  init {
    doOnInflate {
      binding.uiToolbar.setNavigationOnClickListener(
          DebouncedOnClickListener.create { publish(SymbolAddViewEvent.Close) })
    }

    doOnTeardown { clear() }

    doOnInflate { binding.uiToolbar.inflateMenu(R.menu.add) }

    doOnInflate {
      binding.uiToolbar.setOnMenuItemClickListener { item ->
        return@setOnMenuItemClickListener when (item.itemId) {
          R.id.menu_symbol_add -> {
            publish(SymbolAddViewEvent.CommitSymbol)
            true
          }
          else -> false
        }
      }
    }
  }

  override fun onRender(state: UiRender<SymbolAddViewState>) {
    state.mapChanged { it.type }.render(viewScope) { handleType(it) }
  }

  private fun handleType(type: HoldingType) {
    val name =
        when (type) {
          is HoldingType.Crypto -> "Cryptocurrency"
          is HoldingType.Stock -> "Stock"
          is HoldingType.Options.Buy, is HoldingType.Options.Sell -> "Option"
        }
    binding.uiToolbar.title = "Add $name"
  }

  private fun clear() {
    binding.uiToolbar.apply {
      menu.clear()
      setNavigationOnClickListener(null)
      setOnMenuItemClickListener(null)
      title = ""
    }
  }
}
