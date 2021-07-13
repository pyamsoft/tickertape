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
    BaseUiView<PortfolioItemViewState, PortfolioItemViewEvent, HoldingSummaryBinding>(parent) {

  override val viewBinding = HoldingSummaryBinding::inflate

  override val layoutRoot by boundView { holdingSummaryRoot }

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<PortfolioItemViewState>) {
    state.mapChanged { it.stock }.apply {
      mapChanged { it.totalShares() }.render(viewScope) { handleTotalSharesChanged(it) }
      mapChanged { it.gainLossDisplayString() }.render(viewScope) { handleGainLossChanged(it) }
      mapChanged { it.current() }.render(viewScope) { handleCurrentValueChanged(it) }
      mapChanged { it.todayDirection() }.render(viewScope) { handleTodayDirectionChanged(it) }
      mapChanged { it.totalDirection() }.render(viewScope) { handleTotalDirectionChanged(it) }
      mapChanged { it.changeTodayDisplayString() }.render(viewScope) { handleTodayChanged(it) }
    }
  }

  private fun handleTotalDirectionChanged(direction: StockDirection) {
    val color = direction.color()
    binding.holdingSummaryGainlossText.setTextColor(color)
    binding.holdingSummaryGainlossLabel.text =
        when {
          direction.isUp() -> "Total Gain"
          direction.isDown() -> "Total Loss"
          else -> "Total Change"
        }
  }

  private fun handleTodayDirectionChanged(direction: StockDirection) {
    val color = direction.color()
    binding.holdingSummaryChangeText.setTextColor(color)
    binding.holdingSummaryChangeLabel.text =
        when {
          direction.isUp() -> "Gain Today"
          direction.isDown() -> "Loss Today"
          else -> "Change Today"
        }
  }

  private fun clearTotalShares() {
    binding.holdingSummaryTotalSharesText.text = ""
  }

  private fun clearTodayChangePrice() {
    binding.holdingSummaryChangeText.text = ""
  }

  private fun clearGainLossPrice() {
    binding.holdingSummaryGainlossText.text = ""
  }

  private fun clearCurrent() {
    binding.holdingSummaryCurrentValueText.text = ""
  }

  private fun clear() {
    clearTotalShares()
    clearTodayChangePrice()
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

  private fun handleTodayChanged(today: String?) {
    if (today == null) {
      clearTodayChangePrice()
    } else {
      binding.holdingSummaryChangeText.text = today
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
    val direction = totalDirection()
    val gainLoss = totalGainLoss()
    val gainLossPercent = totalGainLossPercent()
    if (gainLoss == null || gainLossPercent == null) {
      return null
    }

    val sign = direction.sign()
    return "${sign}${gainLoss.asMoneyValue()} (${sign}${gainLossPercent.asPercentValue()})"
  }

  @CheckResult
  private fun PortfolioStock.changeTodayDisplayString(): String? {
    val direction = todayDirection()
    val change = todayChange() ?: return null

    val sign = direction.sign()
    return "${sign}${change.asMoneyValue()}"
  }
}
