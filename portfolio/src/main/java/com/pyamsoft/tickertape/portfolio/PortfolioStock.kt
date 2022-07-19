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
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.priceWithSplits
import com.pyamsoft.tickertape.db.position.shareCountWithSplits
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asShares

private const val NO_POSITION = 0.0

data class PortfolioStock
internal constructor(
    val holding: DbHolding,
    val positions: List<DbPosition>,
    val ticker: Ticker?,
    val splits: List<DbSplit>,
) {

  val todayDirection: StockDirection
  val totalDirection: StockDirection
  val current: StockMoneyValue
  val totalShares: StockShareValue
  val gainLossDisplayString: String
  val changeTodayDisplayString: String
  val isOption = holding.type() == EquityType.OPTION

  // Used in PortfolioStockList
  internal val costNumber: Double
  internal val todayChangeNumber: Double?
  internal val todayNumber: Double?

  init {
    val optionsModifier = if (isOption) 100 else 1
    val sellSideModifier = if (holding.side() == TradeSide.SELL) -1 else 1

    val isNoPosition = positions.isEmpty()

    val cost =
        if (isNoPosition) NO_POSITION
        else
            positions.sumOf {
              it.priceWithSplits(splits).value() * it.shareCountWithSplits(splits).value()
            }

    val totalSharesNumber =
        if (isNoPosition) NO_POSITION
        else positions.sumOf { it.shareCountWithSplits(splits).value() }

    // Avoid -0.0 as a total change number
    val tempTodayChange: Double
    val tempTodayNumber: Double
    if (isNoPosition) {
      tempTodayChange = NO_POSITION
      tempTodayNumber = NO_POSITION
    } else {
      val q = ticker?.quote
      if (q == null) {
        tempTodayChange = NO_POSITION
        tempTodayNumber = NO_POSITION
      } else {
        val reg = q.regular()
        tempTodayChange = reg.amount().value() * totalSharesNumber
        tempTodayNumber = reg.price().value() * totalSharesNumber
      }
    }

    val isNoTodayChange = tempTodayChange.isZero()
    todayChangeNumber = tempTodayChange * optionsModifier
    todayNumber = tempTodayNumber * optionsModifier

    val totalGainLoss: StockMoneyValue
    val totalGainLossPercent: StockPercent
    if (isNoPosition) {
      current = StockMoneyValue.none()
      costNumber = NO_POSITION
      totalShares = NO_POSITION.asShares()
      totalDirection = StockDirection.none()
      todayDirection = StockDirection.none()
      totalGainLoss = StockMoneyValue.none()
      totalGainLossPercent = StockPercent.none()
    } else {
      val totalGainLossNumber = tempTodayNumber - cost
      val isNoTotalChange = totalGainLossNumber.isZero()

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
          if (isNoTotalChange) NO_POSITION else totalGainLossNumber / cost * 100
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

  companion object {

    @JvmField
    val COMPARATOR =
        Comparator<PortfolioStock> { s1, s2 ->
          val t1 = s1.ticker
          val t2 = s2.ticker
          if (t1 != null && t2 != null) {
            return@Comparator Ticker.COMPARATOR.compare(t1, t2)
          }

          if (t1 == null) {
            return@Comparator -1
          }

          if (t2 == null) {
            return@Comparator 1
          }

          return@Comparator s1.holding
              .symbol()
              .raw
              .compareTo(s2.holding.symbol().raw, ignoreCase = true)
        }
  }
}
