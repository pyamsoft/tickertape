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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.main.databinding.SymbolAddHoldingTypeBinding
import com.pyamsoft.tickertape.stocks.api.HoldingType
import javax.inject.Inject

class SymbolAddHoldingType @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<SymbolAddViewState, SymbolAddViewEvent, SymbolAddHoldingTypeBinding>(parent) {

  override val viewBinding = SymbolAddHoldingTypeBinding::inflate

  override val layoutRoot by boundView { symbolAddHoldingRoot }

  init {
    doOnInflate {
      binding.symbolAddHoldingType.setOnDebouncedClickListener {
        publish(SymbolAddViewEvent.UpdateType)
      }
    }

    doOnTeardown { binding.symbolAddHoldingType.setOnDebouncedClickListener(null) }
  }

  override fun onRender(state: UiRender<SymbolAddViewState>) {
    state.mapChanged { it.type }.render(viewScope) { handleTypeChanged(it) }
  }

  private fun handleTypeChanged(type: HoldingType) {
    binding.symbolAddHoldingType.text =
        when (type) {
          is HoldingType.Equity -> "Equity"
          is HoldingType.Options.Buy -> "Options Buy"
          is HoldingType.Options.Sell -> "Options Sell"
        }
  }
}
