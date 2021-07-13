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

package com.pyamsoft.tickertape.portfolio

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioHeaderViewBinding
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.ui.getUserMessage
import com.robinhood.ticker.TickerUtils

abstract class BasePortfolioHeader<S : UiViewState> protected constructor(parent: ViewGroup) :
    BaseUiView<S, Nothing, PortfolioHeaderViewBinding>(parent) {

  final override val viewBinding = PortfolioHeaderViewBinding::inflate

  final override val layoutRoot by boundView { portfolioHeaderAppbar }

  init {
    doOnInflate {
      binding.apply {
        portfolioHeaderChangeTodayText.setCharacterLists(TICKER_CHARACTERS)
        portfolioHeaderGainloss.setCharacterLists(TICKER_CHARACTERS)
        portfolioHeaderToday.setCharacterLists(TICKER_CHARACTERS)
      }
    }

    doOnTeardown { clear() }
  }

  protected fun handleRender(state: UiRender<PortfolioViewState>) {
    state.mapChanged { it.portfolio }.render(viewScope) { handleStocks(it) }
    state.mapChanged { it.error }.render(viewScope) { handleError(it) }
  }

  private fun handleError(throwable: Throwable?) {
    if (throwable == null) {
      binding.portfolioHeaderError.isGone = true
      binding.portfolioHeader.isVisible = true
    } else {
      binding.portfolioHeaderError.text = throwable.getUserMessage()
      binding.portfolioHeaderError.isVisible = true
      binding.portfolioHeader.isGone = true
    }
  }

  private fun handleStocks(list: List<PortfolioStock>) {
    // Total
    val totalAmount = list.sumTotalAmount()
    handleTotalAmount(totalAmount)

    list.sumTotalDirection().apply {
      val totalGainLoss = list.sumTotalGainLoss()
      val totalPercent = list.sumTotalPercent()
      val color = this.color()

      handleGainLoss(this.gainLossDisplayString(totalGainLoss, totalPercent), color)
    }

    // Today
    list.sumTodayDirection().apply {
      val todayChange = list.sumTodayChange()
      val todayPercent = list.sumTodayPercent()
      val color = this.color()

      handleChangeToday(this.gainLossDisplayString(todayChange, todayPercent), color)
    }
  }

  private fun clear() {
    binding.apply {
      portfolioHeaderToday.text = ""
      portfolioHeaderGainloss.text = ""
      portfolioHeaderChangeTodayText.text = ""
    }
  }

  private fun handleChangeToday(change: String, @ColorInt color: Int) {
    binding.portfolioHeaderChangeTodayText.apply {
      text = change
      textColor = color
    }
  }

  private fun handleTotalAmount(today: StockMoneyValue) {
    binding.portfolioHeaderToday.text = today.asMoneyValue()
  }

  private fun handleGainLoss(gainLoss: String, @ColorInt color: Int) {
    binding.portfolioHeaderGainloss.apply {
      text = gainLoss
      textColor = color
    }
  }

  companion object {

    private val TICKER_CHARACTERS = "($+-${TickerUtils.provideNumberList()}%)"

    @JvmStatic
    @CheckResult
    private fun StockDirection.gainLossDisplayString(
        amount: StockMoneyValue,
        percent: StockPercent
    ): String {
      val sign = this.sign()
      return "${sign}${amount.asMoneyValue()} (${sign}${percent.asPercentValue()})"
    }
  }
}
