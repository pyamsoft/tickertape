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
import com.pyamsoft.tickertape.db.position.isLongTerm
import com.pyamsoft.tickertape.db.position.isShortTerm
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
import java.time.LocalDate

private const val NO_POSITION = 0.0

data class PortfolioStock
internal constructor(
    val holding: DbHolding,
    val positions: List<DbPosition>,
    val ticker: Ticker?,
    val splits: List<DbSplit>,
) {
  val isOption = holding.type == EquityType.OPTION

  val current: StockMoneyValue

  val totalDirection: StockDirection
  val totalShares: StockShareValue
  val totalGainLossAmount: String
  val totalGainLossPercent: String
  val overallCostBasis: StockMoneyValue

  val shortTermPositions: Int
  val longTermPositions: Int

  // Used in PortfolioStockList
  internal val costNumber: Double
  internal val todayChangeNumber: Double?
  internal val todayNumber: Double?

  init {
    val isSell = holding.side == TradeSide.SELL
    val optionsModifier = if (isOption) 100 else 1
    val sellSideModifier = if (isSell) -1 else 1

    val isNoPosition = positions.isEmpty()

    val cost =
        if (isNoPosition) NO_POSITION
        else
            positions.sumOf {
              it.priceWithSplits(splits).value * it.shareCountWithSplits(splits).value
            }

    val totalSharesNumber =
        if (isNoPosition) NO_POSITION else positions.sumOf { it.shareCountWithSplits(splits).value }

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
        val session = q.currentSession
        tempTodayChange = session.amount.value * totalSharesNumber
        tempTodayNumber = session.price.value * totalSharesNumber
      }
    }

    todayChangeNumber = tempTodayChange * optionsModifier
    todayNumber = tempTodayNumber * optionsModifier

    val totalGainLoss: StockMoneyValue
    val totalGainLossPercent: StockPercent
    if (isNoPosition) {
      current = StockMoneyValue.NONE
      costNumber = NO_POSITION
      totalShares = NO_POSITION.asShares()
      totalDirection = StockDirection.NONE
      totalGainLoss = StockMoneyValue.NONE
      totalGainLossPercent = StockPercent.NONE
      overallCostBasis = StockMoneyValue.NONE
    } else {
      val totalGainLossNumber = tempTodayNumber - cost
      val isNoTotalChange = totalGainLossNumber.isZero()

      totalGainLoss =
          if (isNoTotalChange) StockMoneyValue.NONE
          else (totalGainLossNumber * optionsModifier * sellSideModifier).asMoney()
      totalDirection =
          if (isNoTotalChange) StockDirection.NONE
          else (totalGainLossNumber * sellSideModifier).asDirection()
      current = (tempTodayNumber * sellSideModifier * optionsModifier).asMoney()
      costNumber = cost
      totalShares = (totalSharesNumber * sellSideModifier).asShares()

      val totalGainLossPercentNumber =
          if (isNoTotalChange) NO_POSITION else totalGainLossNumber / cost * 100
      totalGainLossPercent = (totalGainLossPercentNumber * sellSideModifier).asPercent()

      // Overall cost basis is the sum of all cost / number of shares received, adjusted for the
      // trade side
      overallCostBasis = (cost / totalSharesNumber * sellSideModifier).asMoney()
    }

    val sign = totalDirection.sign
    totalGainLossAmount = "${sign}${totalGainLoss.display}"
    this.totalGainLossPercent = "${sign}${totalGainLossPercent.display}"

    if (isNoPosition) {
      shortTermPositions = 0
      longTermPositions = 0
    } else {
      val today = LocalDate.now()
      shortTermPositions = positions.count { it.isShortTerm(today) }
      longTermPositions = positions.count { it.isLongTerm(today) }
    }
  }

  companion object {

    @JvmField
    val COMPARATOR =
        Comparator<PortfolioStock> { o1, o2 ->
          val t1 = o1.ticker
          val t2 = o2.ticker

          if (t1 != null && t2 != null) {
            return@Comparator Ticker.COMPARATOR.compare(t1, t2)
          }

          if (t1 == null) {
            return@Comparator -1
          }

          if (t2 == null) {
            return@Comparator 1
          }

          return@Comparator o2.holding.symbol.compareTo(o1.holding.symbol)
        }
  }
}
