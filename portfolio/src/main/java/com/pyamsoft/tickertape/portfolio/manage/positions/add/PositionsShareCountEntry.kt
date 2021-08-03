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
import com.google.android.material.textfield.TextInputEditText
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.HoldingSharesEntryBinding
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asShares
import com.pyamsoft.tickertape.stocks.api.isOption
import javax.inject.Inject

class PositionsShareCountEntry @Inject internal constructor(type: HoldingType, parent: ViewGroup) :
    BasePositionsEditable<HoldingSharesEntryBinding>(parent) {

  override val viewBinding = HoldingSharesEntryBinding::inflate

  override val layoutRoot by boundView { positionSharesRoot }

  init {
    doOnInflate {
      binding.positionSharesInput.hint =
          "Number of ${if (type.isOption()) "Contracts" else "Shares"}"
    }
  }

  override fun provideEditText(): TextInputEditText {
    return binding.positionSharesEdit
  }

  override fun provideEvent(value: Double): PositionsAddViewEvent {
    return PositionsAddViewEvent.UpdateNumberOfShares(value.asShares())
  }

  override fun onRender(state: UiRender<PositionsAddViewState>) {
    state.mapChanged { it.numberOfShares }.render(viewScope) { handleCountChanged(it) }
  }

  private fun handleCountChanged(value: StockShareValue) {
    // Use asShareValue() so we output a double when fractional but otherwise a whole number
    handleValueChanged(if (value.isZero()) "" else value.asShareValue())
  }
}
