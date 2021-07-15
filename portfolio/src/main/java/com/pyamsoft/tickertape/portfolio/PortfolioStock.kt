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

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asShares

data class PortfolioStock
internal constructor(
    val holding: DbHolding,
    val positions: List<DbPosition>,
    val quote: QuotedStock?,
) {

  private val totalGainLossPercentNumber: Double?
  internal val costNumber = positions.sumOf { it.price().value() * it.shareCount().value() }
  private val totalSharesNumber = positions.sumOf { it.shareCount().value() }
  internal val todayChangeNumber =
      quote?.quote?.regular()?.amount()?.value()?.times(totalSharesNumber)
  val todayDirection = todayChangeNumber?.asDirection() ?: StockDirection.none()
  val todayChange = todayChangeNumber?.asMoney() ?: StockMoneyValue.none()
  internal val todayNumber = quote?.quote?.regular()?.price()?.value()?.times(totalSharesNumber)
  private val totalGainLossNumber = todayNumber?.minus(costNumber)
  val totalDirection = totalGainLossNumber?.asDirection() ?: StockDirection.none()
  val totalGainLossPercent: StockPercent
  val totalGainLoss: StockMoneyValue
  val current = todayNumber?.asMoney() ?: StockMoneyValue.none()
  val totalShares = totalSharesNumber.asShares()

  val gainLossDisplayString: String
  val changeTodayDisplayString: String

  init {
    val totalCost = costNumber
    totalGainLossPercentNumber =
        totalGainLossNumber?.let { if (totalCost.compareTo(0) == 0) 0.0 else it / totalCost * 100 }

    totalGainLossPercent = totalGainLossPercentNumber?.asPercent() ?: StockPercent.none()
    totalGainLoss = totalGainLossNumber?.asMoney() ?: StockMoneyValue.none()

    val direction = totalDirection
    val gainLoss = totalGainLoss
    val gainLossPercent = totalGainLossPercent
    val sign = direction.sign()
    gainLossDisplayString =
        "${sign}${gainLoss.asMoneyValue()} (${sign}${gainLossPercent.asPercentValue()})"

    val change = todayChange
    changeTodayDisplayString = "${sign}${change.asMoneyValue()}"
  }
}

@CheckResult
private fun List<PortfolioStock>.sumCostNumber(): Double {
  return this.map { it.costNumber }.sum()
}

@CheckResult
private fun List<PortfolioStock>.sumTotalAmountNumber(): Double? {
  val todays = this.map { it.todayNumber }
  return if (todays.any { it == null }) null else todays.filterNotNull().sum()
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalAmount(): StockMoneyValue {
  return this.sumTotalAmountNumber()?.asMoney() ?: StockMoneyValue.none()
}

@CheckResult
private fun List<PortfolioStock>.sumTotalGainLossNumber(): Double? {
  val today = this.sumTotalAmountNumber() ?: return null
  return today - this.sumCostNumber()
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalGainLoss(): StockMoneyValue {
  return this.sumTotalGainLossNumber()?.asMoney() ?: StockMoneyValue.none()
}

@CheckResult
private fun List<PortfolioStock>.sumTotalPercentNumber(): Double? {
  val gainLoss = this.sumTotalGainLossNumber() ?: return null
  val cost = this.sumCostNumber()
  return if (cost.compareTo(0) == 0) 0.0 else gainLoss / cost * 100
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalPercent(): StockPercent {
  return this.sumTotalPercentNumber()?.asPercent() ?: StockPercent.none()
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalDirection(): StockDirection {
  val today = this.sumTotalAmountNumber() ?: return StockDirection.none()
  val total = this.sumCostNumber()
  return (today - total).asDirection()
}

@CheckResult
private fun List<PortfolioStock>.sumTodayChangeNumber(): Double? {
  val todays = this.map { it.todayChangeNumber }
  return if (todays.any { it == null }) null
  else {
    todays.filterNotNull().sum()
  }
}

@CheckResult
internal fun List<PortfolioStock>.sumTodayChange(): StockMoneyValue {
  return this.sumTodayChangeNumber()?.asMoney() ?: StockMoneyValue.none()
}

@CheckResult
private fun List<PortfolioStock>.sumTodayPercentNumber(): Double? {
  val change = this.sumTodayChangeNumber() ?: return null
  val total = this.sumTotalAmountNumber() ?: return null
  return if (total.compareTo(0) == 0) 0.0 else change / total * 100
}

@CheckResult
internal fun List<PortfolioStock>.sumTodayPercent(): StockPercent {
  return this.sumTodayPercentNumber()?.asPercent() ?: StockPercent.none()
}

@CheckResult
internal fun List<PortfolioStock>.sumTodayDirection(): StockDirection {
  return this.sumTodayChangeNumber()?.asDirection() ?: StockDirection.none()
}
