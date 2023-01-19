package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.priceWithSplits
import com.pyamsoft.tickertape.db.position.shareCountWithSplits
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent

@Stable
data class PositionStock
internal constructor(
    private val position: DbPosition,
    val equityType: EquityType,
    val tradeSide: TradeSide,
    val splits: List<DbSplit>,
    val currentPrice: StockMoneyValue?
) : DbPosition by position {

  val isOption: Boolean = equityType == EquityType.OPTION
  val isSell: Boolean = tradeSide == TradeSide.SELL

  val cost: String
  val currentValue: String
  val gainLossAmount: Double
  val gainLossPercent: Double
  val displayGainLossAmount: String
  val displayGainLossPercent: String
  val color: Color

  init {

    val optionsModifier = if (isOption) 100 else 1
    val sellSideModifier = if (isSell) -1 else 1

    val price = position.priceWithSplits(splits)
    val shareCount = position.shareCountWithSplits(splits)
    val totalCost = price.value * shareCount.value
    val displayTotal = (totalCost * optionsModifier).asMoney().display

    val gainLossAmount: Double
    val gainLossPercent: Double
    val displayGainLossAmount: String
    val displayGainLossPercent: String
    val displayCurrent: String
    val colorGainLoss: Color

    if (currentPrice == null) {
      gainLossAmount = 0.0
      gainLossPercent = 0.0
      displayCurrent = ""
      displayGainLossAmount = ""
      displayGainLossPercent = ""
      colorGainLoss = Color.Unspecified
    } else {
      // Current
      val currentRaw = (currentPrice.value * shareCount.value)
      val currentValue = currentRaw.asMoney()
      displayCurrent = currentValue.display

      // Gain/Loss
      val gainLossValue = currentValue.value - totalCost
      val gainLossPct = (gainLossValue * 100) / totalCost

      val optionsScaledValue = gainLossValue * optionsModifier
      val sideScaledValue = optionsScaledValue * sellSideModifier
      val sideScaledPct = gainLossPct * sellSideModifier

      val gainLossDirection = sideScaledValue.asDirection()
      val sign = gainLossDirection.sign

      // Final display
      gainLossAmount = sideScaledValue
      gainLossPercent = sideScaledPct
      displayGainLossAmount = "${sign}${gainLossAmount.asMoney().display}"
      displayGainLossPercent = "${sign}${gainLossPercent.asPercent().display}"
      colorGainLoss = Color(gainLossDirection.color)
    }

    this.cost = displayTotal
    this.currentValue = displayCurrent
    this.gainLossAmount = gainLossAmount
    this.gainLossPercent = gainLossPercent
    this.displayGainLossAmount = displayGainLossAmount
    this.displayGainLossPercent = displayGainLossPercent
    this.color = colorGainLoss
  }
}
