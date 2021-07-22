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

package com.pyamsoft.tickertape.portfolio.manage.positions.add

import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputEditText
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.HoldingPriceEntryBinding
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.isOption
import javax.inject.Inject
import timber.log.Timber

class PositionsPriceEntry @Inject internal constructor(type: HoldingType, parent: ViewGroup) :
    BasePositionsEditable<HoldingPriceEntryBinding>(parent) {

  override val viewBinding = HoldingPriceEntryBinding::inflate

  override val layoutRoot by boundView { positionPriceRoot }

  init {
    doOnInflate {
      binding.positionPriceInput.hint = "Price per ${if (type.isOption()) "Contract" else "Share"}"
    }

    doOnInflate {
      binding.positionPriceEdit.setOnEditorActionListener { _, actionId, _ ->
        Timber.d("Enter key pushed, open date picker $actionId")
        if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
          publish(PositionsAddViewEvent.OpenDatePicker)
          return@setOnEditorActionListener true
        }

        return@setOnEditorActionListener false
      }
    }
  }

  override fun provideEditText(): TextInputEditText {
    return binding.positionPriceEdit
  }

  override fun provideEvent(value: Double): PositionsAddViewEvent {
    return PositionsAddViewEvent.UpdateSharePrice(value.asMoney())
  }

  override fun onRender(state: UiRender<PositionsAddViewState>) {
    state.mapChanged { it.pricePerShare }.render(viewScope) { handleValueChanged(it) }
  }
}
