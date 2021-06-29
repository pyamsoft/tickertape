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
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.util.applyAppBarOffset
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioHeaderViewBinding
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.robinhood.ticker.TickerUtils
import javax.inject.Inject

class PortfolioHeader
@Inject
internal constructor(parent: ViewGroup, owner: LifecycleOwner, appBarActivity: AppBarActivity) :
    BaseUiView<PortfolioViewState, Nothing, PortfolioHeaderViewBinding>(parent) {

  override val viewBinding = PortfolioHeaderViewBinding::inflate

  override val layoutRoot by boundView { portfolioHeaderAppbar }

  init {
    doOnInflate { binding.portfolioHeaderCollapse.applyAppBarOffset(appBarActivity, owner) }
    doOnInflate {
      binding.apply {
        portfolioHeaderChangeTodayText.setCharacterLists(TICKER_CHARACTERS)
        portfolioHeaderGainloss.setCharacterLists(TICKER_CHARACTERS)
        portfolioHeaderToday.setCharacterLists(TICKER_CHARACTERS)
      }
    }

    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<PortfolioViewState>) {
    state.mapChanged { it.portfolio }.render(viewScope) { handleRender(it) }
  }

  private fun handleRender(list: List<PortfolioStock>) {
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

  private fun handleChangeToday(change: String?, @ColorInt color: Int) {
    val isMissing = change == null
    binding.apply {
      portfolioHeaderChangeTodayLabel.isInvisible = isMissing
      portfolioHeaderChangeTodayText.isInvisible = isMissing

      if (change != null) {
        portfolioHeaderChangeTodayText.apply {
          text = change
          textColor = color
        }
      }
    }
  }

  private fun handleTotalAmount(today: StockMoneyValue?) {
    val isMissing = today == null
    binding.apply {
      portfolioHeaderToday.isInvisible = isMissing

      if (today != null) {
        portfolioHeaderToday.apply { text = today.asMoneyValue() }
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
          textColor = color
        }
      }
    }
  }

  companion object {

    private val TICKER_CHARACTERS = "($+-${TickerUtils.provideNumberList()}%)"

    @JvmStatic
    @CheckResult
    private fun StockDirection.gainLossDisplayString(
        amount: StockMoneyValue?,
        percent: StockPercent?
    ): String? {
      if (amount == null || percent == null) {
        return null
      }

      val sign = this.sign()
      return "${sign}${amount.asMoneyValue()} (${sign}${percent.asPercentValue()})"
    }
  }
}
