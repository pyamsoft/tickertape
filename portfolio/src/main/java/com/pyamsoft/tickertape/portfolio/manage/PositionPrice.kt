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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.PositionSharePriceBinding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.ui.UiEditTextDelegate
import javax.inject.Inject
import timber.log.Timber

class PositionPrice @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<ManagePortfolioViewState, ManagePortfolioViewEvent, PositionSharePriceBinding>(
        parent) {

  override val viewBinding = PositionSharePriceBinding::inflate

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
            val sharePrice = numberString.toFloatOrNull()
            if (sharePrice == null) {
              Timber.w("Invalid sharePrice $numberString")
              return@create false
            }

            publish(ManagePortfolioViewEvent.UpdateSharePrice(sharePrice.asMoney()))
            return@create true
          }
              .apply { handleCreate() }
    }
  }

  override fun onRender(state: UiRender<ManagePortfolioViewState>) {
    state.mapChanged { it.pricePerShare }.render(viewScope) { handleSharePriceChanged(it) }
  }

  private fun handleSharePriceChanged(sharePrice: StockMoneyValue) {
    // Don't use asMoneyValue() here since we do not want to include the $ and stuff
    val text = if (sharePrice.isZero()) "" else sharePrice.value().toString()
    requireNotNull(delegate).handleTextChanged(text)
  }
}
