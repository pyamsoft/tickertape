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
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.HoldingQuoteBinding
import com.pyamsoft.tickertape.quote.clearSession
import com.pyamsoft.tickertape.quote.populateSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import javax.inject.Inject

class HoldingQuote @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<HoldingViewState, HoldingViewEvent, HoldingQuoteBinding>(parent) {

  override val viewBinding = HoldingQuoteBinding::inflate

  override val layoutRoot by boundView { positionQuoteRoot }

  init {
    doOnTeardown { clear() }
  }

  private fun clear() {
    binding.positonQuoteAfterHours.isInvisible = true

    clearSession(binding.positionQuoteAhSession)
    clearSession(binding.positionQuoteNormalSession)
  }

  override fun onRender(state: UiRender<HoldingViewState>) {
    state.mapChanged { it.stock }.mapChanged { it?.quote }.mapChanged { it?.quote }.render(
        viewScope) { handleQuoteChanged(it) }
  }

  private fun handleQuoteChanged(quote: StockQuote?) {
    if (quote == null) {
      clear()
    } else {
      populateSession(binding.positionQuoteNormalSession, quote.regular())

      val afterHours = quote.afterHours()
      if (afterHours == null) {
        binding.positonQuoteAfterHours.isInvisible = true
      } else {
        binding.positonQuoteAfterHours.isVisible = true
        populateSession(binding.positionQuoteAhSession, afterHours)
      }
    }
  }
}
