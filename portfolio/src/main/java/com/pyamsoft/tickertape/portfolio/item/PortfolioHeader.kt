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
import androidx.annotation.ColorInt
import androidx.core.view.isInvisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioHeaderViewBinding
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import javax.inject.Inject

class PortfolioHeader @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PortfolioItemViewState.Header, Nothing, PortfolioHeaderViewBinding>(parent) {

  override val viewBinding = PortfolioHeaderViewBinding::inflate

  override val layoutRoot by boundView { portfolioHeaderRoot }

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<PortfolioItemViewState.Header>) {
    state.render(viewScope) { handleRender(it) }
  }

  private fun handleRender(state: PortfolioItemViewState.Header) {
    val direction = state.totalDirection
    val color = direction.color()
    handleToday(state.totalToday, color)
    handleGainLoss(state.gainLossDisplayString(direction), color)

    handleCost(state.totalCost)
  }

  private fun clear() {
    binding.apply {
      portfolioHeaderToday.text = ""
      portfolioHeaderCostText.text = ""
      portfolioHeaderGainloss.text = ""
    }
  }

  private fun handleToday(today: StockMoneyValue?, @ColorInt color: Int) {
    val isMissing = today == null
    binding.apply {
      portfolioHeaderToday.isInvisible = isMissing

      if (today != null) {
        portfolioHeaderToday.apply {
          text = today.asMoneyValue()
          setTextColor(color)
        }
      }
    }
  }

  private fun handleGainLoss(gainLoss: String?, @ColorInt color: Int) {
    val isMissing = gainLoss == null
    binding.apply {
      portfolioHeaderGainloss.isInvisible = isMissing

      if (gainLoss != null) {
        portfolioHeaderGainloss.apply {
          text = gainLoss
          setTextColor(color)
        }
      }
    }
  }

  private fun handleCost(cost: StockMoneyValue) {
    binding.portfolioHeaderCostText.text = cost.asMoneyValue()
  }

  @CheckResult
  private fun PortfolioItemViewState.Header.gainLossDisplayString(
      direction: StockDirection
  ): String? {
    val gainLoss = totalGainLoss
    val gainLossPercent = totalPercent
    if (gainLoss == null || gainLossPercent == null) {
      return null
    }

    val sign = direction.sign()
    return "${sign}${gainLoss.asMoneyValue()} (${sign}${gainLossPercent.asPercentValue()})"
  }
}
