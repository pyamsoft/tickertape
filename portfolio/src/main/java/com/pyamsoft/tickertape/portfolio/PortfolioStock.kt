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

import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.isOption
import com.pyamsoft.tickertape.db.holding.isSellSide
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockShareValue
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

  val todayDirection: StockDirection
  val totalDirection: StockDirection
  val current: StockMoneyValue
  val totalShares: StockShareValue
  val gainLossDisplayString: String
  val changeTodayDisplayString: String
  val isOption = holding.isOption()

  // Used in PortfolioStockList
  internal val costNumber: Double
  internal val todayChangeNumber: Double?
  internal val todayNumber: Double?

  init {
    val optionsModifier = if (isOption) 100 else 1
    val sellSideModifier = if (holding.isSellSide()) -1 else 1

    val isNoPosition = positions.isEmpty()
    val cost =
        if (isNoPosition) 0.0 else positions.sumOf { it.price().value() * it.shareCount().value() }
    val totalSharesNumber = if (isNoPosition) 0.0 else positions.sumOf { it.shareCount().value() }

    // Avoid -0.0 as a total change number
    val tempTodayChange: Double
    val tempTodayNumber: Double
    if (isNoPosition) {
      tempTodayChange = 0.0
      tempTodayNumber = 0.0
    } else {
      val q = quote?.quote
      if (q == null) {
        tempTodayChange = 0.0
        tempTodayNumber = 0.0
      } else {
        tempTodayChange = q.regular().amount().value() * totalSharesNumber
        tempTodayNumber = q.regular().price().value() * totalSharesNumber
      }
    }

    val totalGainLossNumber = tempTodayNumber - cost
    val isNoTotalChange = totalGainLossNumber.isZero()
    val isNoTodayChange = tempTodayChange.isZero()

    todayChangeNumber = tempTodayChange * optionsModifier
    todayNumber = tempTodayNumber * optionsModifier

    val totalGainLoss: StockMoneyValue
    val totalGainLossPercent: StockPercent
    if (isNoPosition) {
      current = StockMoneyValue.none()
      costNumber = 0.0
      totalShares = 0.0.asShares()
      totalDirection = StockDirection.none()
      todayDirection = StockDirection.none()
      totalGainLoss = StockMoneyValue.none()
      totalGainLossPercent = StockPercent.none()
    } else {
      totalGainLoss =
          if (isNoTotalChange) StockMoneyValue.none()
          else (totalGainLossNumber * optionsModifier * sellSideModifier).asMoney()
      totalDirection =
          if (isNoTotalChange) StockDirection.none()
          else (totalGainLossNumber * sellSideModifier).asDirection()
      todayDirection =
          if (isNoTodayChange) StockDirection.none()
          else (tempTodayChange * sellSideModifier).asDirection()
      current = (tempTodayNumber * sellSideModifier * optionsModifier).asMoney()
      costNumber = cost
      totalShares = (totalSharesNumber * sellSideModifier).asShares()

      val totalGainLossPercentNumber =
          if (isNoTotalChange) 0.0 else totalGainLossNumber / cost * 100
      totalGainLossPercent = (totalGainLossPercentNumber * sellSideModifier).asPercent()
    }

    val sign = totalDirection.sign()
    gainLossDisplayString =
        "${sign}${totalGainLoss.asMoneyValue()} (${sign}${totalGainLossPercent.asPercentValue()})"

    val todayChange =
        if (isNoTodayChange) StockMoneyValue.none()
        else (tempTodayChange * sellSideModifier * optionsModifier).asMoney()
    changeTodayDisplayString = "${todayDirection.sign()}${todayChange.asMoneyValue()}"
  }
}
