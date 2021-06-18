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

package com.pyamsoft.tickertape.portfolio.manage.positions.holding

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.HoldingPriceEntryBinding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.ui.UiEditTextDelegate
import javax.inject.Inject
import timber.log.Timber

class HoldingPriceEntry @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<HoldingViewState, HoldingViewEvent, HoldingPriceEntryBinding>(parent) {

  override val viewBinding = HoldingPriceEntryBinding::inflate

  override val layoutRoot by boundView { positionPriceRoot }

  private var delegate: UiEditTextDelegate? = null

  init {
    doOnTeardown {
      delegate?.handleTeardown()
      delegate = null
    }

    doOnInflate {
      delegate =
          UiEditTextDelegate.create(binding.positionPriceEdit) { numberString ->
            // Blank string reset to 0
            if (numberString.isBlank()) {
              publish(HoldingViewEvent.UpdateSharePrice(StockMoneyValue.none()))
              return@create true
            }

            val sharePrice = numberString.toDoubleOrNull()
            if (sharePrice == null) {
              Timber.w("Invalid sharePrice $numberString")
              return@create false
            }

            publish(HoldingViewEvent.UpdateSharePrice(sharePrice.asMoney()))
            return@create true
          }
              .apply { handleCreate() }
    }
  }

  override fun onRender(state: UiRender<HoldingViewState>) {
    state.mapChanged { it.pricePerShare }.render(viewScope) { handleSharePriceChanged(it) }
  }

  private fun handleSharePriceChanged(sharePrice: StockMoneyValue) {
    // Don't use asMoneyValue() here since we do not want to include the $ and stuff
    val text = if (sharePrice.isZero()) "" else sharePrice.value().toString()
    requireNotNull(delegate).handleTextChanged(text)
  }
}
