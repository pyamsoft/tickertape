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

package com.pyamsoft.tickertape.portfolio.manage.positions.item

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import java.time.LocalDateTime
import javax.inject.Inject

class PositionItemView @Inject internal constructor(parent: ViewGroup) :
    BasePositionItemView<PositionItemViewState.Position>(parent) {

  init {
    doOnInflate {
      binding.positionItem.setOnLongClickListener {
        publish(PositionItemViewEvent.Remove)
        return@setOnLongClickListener true
      }
    }

    doOnTeardown { binding.positionItem.setOnLongClickListener(null) }
  }

  override fun onRender(state: UiRender<PositionItemViewState.Position>) {
    state.mapChanged { it.isOption }.render(viewScope) { handleOptionChanged(it) }
    state.mapChanged { it.positionSize }.render(viewScope) { handleShareCountChanged(it) }
    state.mapChanged { it.positionCost }.render(viewScope) { handleSharePriceChanged(it) }
    state.mapChanged { it.position }.mapChanged { it.purchaseDate() }.render(viewScope) {
      handlePurchaseDateChanged(it)
    }

    state.mapChanged { it.total }.render(viewScope) { handleTotalChanged(it) }
    state.mapChanged { it.gainLossDisplayString }.render(viewScope) { handleGainLossChanged(it) }
    state.mapChanged { it.gainLossDirection }.render(viewScope) {
      handleGainLossDirectionChanged(it)
    }
  }

  private fun handleOptionChanged(isOption: Boolean) {
      binding.positionItemNumberOfSharesLabel.text = if (isOption) "Contracts" else "Shares"
  }

  private fun handlePurchaseDateChanged(date: LocalDateTime) {
    binding.positionItemDate.text = DATE_FORMATTER.get().requireNotNull().format(date)
  }

  private fun handleGainLossDirectionChanged(direction: StockDirection) {
    binding.positionItemChange.setTextColor(direction.color())
  }

  private fun handleGainLossChanged(gainLoss: String) {
    handleGainLossVisibilityChanged(gainLoss.isNotBlank())
    handleGainLossTextChanged(gainLoss)
  }

  private fun handleGainLossTextChanged(gainLoss: String) {
    binding.positionItemChange.text = gainLoss
  }

  private fun handleTotalChanged(total: StockMoneyValue) {
    binding.positionItemTotal.text = total.asMoneyValue()
  }

  private fun handleShareCountChanged(shareCount: StockShareValue) {
    binding.positionItemNumberOfShares.text = shareCount.asShareValue()
  }

  private fun handleSharePriceChanged(price: StockMoneyValue) {
    binding.positionItemSharePrice.text = price.asMoneyValue()
  }
}
