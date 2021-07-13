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
import com.pyamsoft.tickertape.portfolio.databinding.HoldingShareCountEntryBinding
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asShares
import com.pyamsoft.tickertape.ui.UiEditTextDelegate
import javax.inject.Inject
import timber.log.Timber

class PositionsShareCountEntry @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PositionsAddViewState, PositionsAddViewEvent, HoldingShareCountEntryBinding>(
        parent) {

  override val viewBinding = HoldingShareCountEntryBinding::inflate

  override val layoutRoot by boundView { positionNumberOfSharesRoot }

  private var delegate: UiEditTextDelegate? = null

  init {
    doOnTeardown {
      delegate?.handleTeardown()
      delegate = null
    }

    doOnInflate {
      delegate =
          UiEditTextDelegate.create(binding.positionNumberOfSharesEdit) { numberString ->
            // Blank string reset to 0
            if (numberString.isBlank()) {
              publish(PositionsAddViewEvent.UpdateNumberOfShares(StockShareValue.none()))
              return@create true
            }

            val numberOfShares = numberString.toDoubleOrNull()
            if (numberOfShares == null) {
              Timber.w("Invalid numberOfShares $numberString")
              return@create false
            }

            publish(PositionsAddViewEvent.UpdateNumberOfShares(numberOfShares.asShares()))
            return@create true
          }
              .apply { handleCreate() }
    }
  }

  override fun onRender(state: UiRender<PositionsAddViewState>) {
    state.mapChanged { it.numberOfShares }.render(viewScope) { handleNumberOfSharesChanged(it) }
  }

  private fun handleNumberOfSharesChanged(numberOfShares: StockShareValue) {
    // Don't use asShareValue() here to avoid getting a format string with commas and stuff
    val text = if (numberOfShares.isZero()) "" else numberOfShares.value().toString()
    requireNotNull(delegate).handleTextChanged(text)
  }
}
