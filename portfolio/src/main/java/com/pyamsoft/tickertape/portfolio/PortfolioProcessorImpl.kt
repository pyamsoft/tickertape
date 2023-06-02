/*
 * Copyright 2023 pyamsoft
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
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class PortfolioProcessorImpl @Inject internal constructor() : PortfolioProcessor {

  @CheckResult
  private fun Sequence<PortfolioStock>.processEquities(): PortfolioData.Data {
    val list = this

    val totalShortTermPositions: Int
    val totalLongTermPositions: Int

    val current: StockMoneyValue
    val totalChange: StockMoneyValue
    val totalChangePercent: StockPercent
    val totalDirection: StockDirection
    val todayChange: StockMoneyValue
    val todayChangePercent: StockPercent
    val todayDirection: StockDirection

    val totalValues = list.map { it.todayNumber }
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
      val totalCost = list.sumOf { it.costNumber }
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

        val todayValues = list.map { it.todayChangeNumber }
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

      totalShortTermPositions = list.sumOf { it.shortTermPositions }
      totalLongTermPositions = list.sumOf { it.longTermPositions }
    }

    return PortfolioData.Data(
        current = current,
        total =
            PortfolioData.Data.Summary(
                change = totalChange,
                changePercent = totalChangePercent,
                direction = totalDirection,
            ),
        today =
            PortfolioData.Data.Summary(
                change = todayChange,
                changePercent = todayChangePercent,
                direction = todayDirection,
            ),
        positions =
            PortfolioData.Data.Positions(
                shortTerm = totalShortTermPositions,
                longTerm = totalLongTermPositions,
            ),
    )
  }

  override suspend fun process(portfolio: List<PortfolioStock>): PortfolioData =
      withContext(context = Dispatchers.Default) {
        val list = portfolio.asSequence()
        return@withContext PortfolioData(
            stocks = list.filter { it.holding.type == EquityType.STOCK }.processEquities(),
            options = list.filter { it.holding.type == EquityType.OPTION }.processEquities(),
            crypto = list.filter { it.holding.type == EquityType.CRYPTOCURRENCY }.processEquities(),
        )
      }
}
