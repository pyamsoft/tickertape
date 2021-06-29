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

package com.pyamsoft.tickertape.portfolio.item

import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.databinding.HoldingSummaryBinding
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import javax.inject.Inject

class PortfolioItemSummary @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PortfolioItemViewState, PortfolioItemViewEvent, HoldingSummaryBinding>(
        parent) {

  override val viewBinding = HoldingSummaryBinding::inflate

  override val layoutRoot by boundView { holdingSummaryRoot }

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<PortfolioItemViewState>) {
    state.mapChanged { it.stock }.apply {
      mapChanged { it.totalShares() }.render(viewScope) { handleTotalSharesChanged(it) }
      mapChanged { it.averagePrice() }.render(viewScope) { handleAveragePriceChanged(it) }
      mapChanged { it.gainLossDisplayString() }.render(viewScope) { handleGainLossChanged(it) }
      mapChanged { it.current() }.render(viewScope) { handleCurrentValueChanged(it) }
      mapChanged { it.todayDirection() }.render(viewScope) { handleDirectionChanged(it) }
    }
  }

  private fun handleDirectionChanged(direction: StockDirection) {
    val color = direction.color()
    binding.holdingSummaryCurrentValueText.setTextColor(color)
    binding.holdingSummaryGainlossText.setTextColor(color)
    binding.holdingSummaryGainlossLabel.text =
        when {
          direction.isUp() -> "Gain"
          direction.isDown() -> "Loss"
          else -> "Change"
        }
  }

  private fun clearTotalShares() {
    binding.holdingSummaryTotalSharesText.text = ""
  }

  private fun clearAveragePrice() {
    binding.holdingSummaryAvgPriceText.text = ""
  }

  private fun clearGainLossPrice() {
    binding.holdingSummaryGainlossText.text = ""
  }

  private fun clearCurrent() {
    binding.holdingSummaryCurrentValueText.text = ""
  }

  private fun clear() {
    clearTotalShares()
    clearAveragePrice()
    clearGainLossPrice()
    clearCurrent()
  }

  private fun handleGainLossChanged(gainLoss: String?) {
    if (gainLoss == null) {
      clearGainLossPrice()
    } else {
      binding.holdingSummaryGainlossText.text = gainLoss
    }
  }

  private fun handleTotalSharesChanged(totalShares: StockShareValue?) {
    if (totalShares == null) {
      clearTotalShares()
    } else {
      binding.holdingSummaryTotalSharesText.text = totalShares.asShareValue()
    }
  }

  private fun handleAveragePriceChanged(averagePrice: StockMoneyValue?) {
    if (averagePrice == null) {
      clearAveragePrice()
    } else {
      binding.holdingSummaryAvgPriceText.text = averagePrice.asMoneyValue()
    }
  }

  private fun handleCurrentValueChanged(current: StockMoneyValue?) {
    if (current == null) {
      clearCurrent()
    } else {
      binding.holdingSummaryCurrentValueText.text = current.asMoneyValue()
    }
  }

  @CheckResult
  private fun PortfolioStock.gainLossDisplayString(): String? {
    val gainLoss = totalGainLoss()
    val gainLossPercent = totalGainLossPercent()
    if (gainLoss == null || gainLossPercent == null) {
      return null
    }

    return "${gainLoss.asMoneyValue()} (${gainLossPercent.asPercentValue()})"
  }
}
