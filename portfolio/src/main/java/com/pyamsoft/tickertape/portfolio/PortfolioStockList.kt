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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import timber.log.Timber

@Stable
class PortfolioStockList
private constructor(
    val list: List<PortfolioStock>,
) {

  @CheckResult
  fun generateData(equityType: EquityType): Data? {
    if (equityType == EquityType.OPTION) {
      Timber.w("Not generating any Data for Options type")
      return null
    }

    val totalShortTermPositions: Int
    val totalLongTermPositions: Int

    val current: StockMoneyValue
    val totalChange: StockMoneyValue
    val totalChangePercent: StockPercent
    val totalDirection: StockDirection
    val todayChange: StockMoneyValue
    val todayChangePercent: StockPercent
    val todayDirection: StockDirection

    val matchingStocks = list.asSequence().filter { it.holding.type == equityType }
    val totalValues = matchingStocks.map { it.todayNumber }
    val isTotalInvalid = totalValues.any { it == null }

    // If we are missing some days, we cannot correctly calculate, so show none
    if (isTotalInvalid) {
      current = StockMoneyValue.NONE
      totalChange = StockMoneyValue.NONE
      totalChangePercent = StockPercent.NONE
      totalDirection = StockDirection.NONE
      todayChange = StockMoneyValue.NONE
      todayChangePercent = StockPercent.NONE
      todayDirection = StockDirection.NONE

      totalShortTermPositions = 0
      totalLongTermPositions = 0
    } else {
      val totalValue = totalValues.filterNotNull().sum()
      current = totalValue.asMoney()

      // If there are no entries, then sum is zero. We cannot divide without getting NaN
      // so we cannot calculate
      val totalCost = matchingStocks.map { it.costNumber }.sum()
      if (totalCost.isZero()) {
        totalChange = StockMoneyValue.NONE
        totalChangePercent = StockPercent.NONE
        totalDirection = StockDirection.NONE
        todayChange = StockMoneyValue.NONE
        todayChangePercent = StockPercent.NONE
        todayDirection = StockDirection.NONE
      } else {
        val rawTotalChange = (totalValue - totalCost)
        totalChange = rawTotalChange.asMoney()
        totalChangePercent = (rawTotalChange / totalCost * 100).asPercent()
        totalDirection = rawTotalChange.asDirection()

        val todayValues = matchingStocks.map { it.todayChangeNumber }
        val isTodayInvalid = todayValues.any { it == null }
        if (isTodayInvalid) {
          todayChange = StockMoneyValue.NONE
          todayChangePercent = StockPercent.NONE
          todayDirection = StockDirection.NONE
        } else {
          val rawTodayChange = todayValues.filterNotNull().sum()
          todayChange = rawTodayChange.asMoney()
          todayChangePercent = (rawTodayChange / totalCost * 100).asPercent()
          todayDirection = rawTodayChange.asDirection()
        }
      }

      totalShortTermPositions = matchingStocks.sumOf { it.shortTermPositions }
      totalLongTermPositions = matchingStocks.sumOf { it.longTermPositions }
    }

    return Data(
        current = current,
        total =
            Data.Summary(
                change = totalChange,
                changePercent = totalChangePercent,
                direction = totalDirection,
            ),
        today =
            Data.Summary(
                change = todayChange,
                changePercent = todayChangePercent,
                direction = todayDirection,
            ),
        positions =
            Data.Positions(
                shortTerm = totalShortTermPositions,
                longTerm = totalLongTermPositions,
            ),
    )
  }

  data class Data(
      val current: StockMoneyValue,
      val total: Summary,
      val today: Summary,
      val positions: Positions,
  ) {

    data class Summary(
        val change: StockMoneyValue,
        val changePercent: StockPercent,
        val direction: StockDirection,
    )

    data class Positions(
        val shortTerm: Int,
        val longTerm: Int,
    )
  }

  companion object {

    private val EMPTY = PortfolioStockList(emptyList())

    @JvmStatic
    @CheckResult
    fun empty(): PortfolioStockList {
      return EMPTY
    }

    @JvmStatic
    @CheckResult
    fun of(list: List<PortfolioStock>): PortfolioStockList {
      return if (list.isEmpty()) EMPTY else PortfolioStockList(list)
    }
  }
}
