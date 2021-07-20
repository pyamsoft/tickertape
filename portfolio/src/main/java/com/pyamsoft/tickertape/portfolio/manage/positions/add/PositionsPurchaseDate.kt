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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.portfolio.databinding.HoldingPurchaseDateBinding
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import java.time.LocalDateTime
import javax.inject.Inject

class PositionsPurchaseDate @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PositionsAddViewState, PositionsAddViewEvent, HoldingPurchaseDateBinding>(parent) {

  override val viewBinding = HoldingPurchaseDateBinding::inflate

  override val layoutRoot by boundView { holdingDateRoot }

  init {
    doOnTeardown { binding.holdingDateText.text = "" }

    doOnInflate {
      binding.holdingDateText.setOnDebouncedClickListener {
        publish(PositionsAddViewEvent.OpenDatePicker)
      }
    }

    doOnTeardown { binding.holdingDateText.setOnDebouncedClickListener(null) }
  }

  override fun onRender(state: UiRender<PositionsAddViewState>) {
    state.mapChanged { it.purchaseDate }.render(viewScope) { handlePurchaseDateChanged(it) }
  }

  private fun handlePurchaseDateChanged(date: LocalDateTime?) {
    binding.holdingDateText.text =
        if (date == null) "--/--/----"
        else DATE_FORMATTER.get().requireNotNull().format(date)
  }
}
