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
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.*

class PortfolioStockList private constructor(val list: List<PortfolioStock>) {

  val sumTotalAmount: StockMoneyValue
  val sumTotalDirection: StockDirection
  val sumTodayDirection: StockDirection
  val gainLossDisplayString: String
  val changeTodayDisplayString: String
  val isEmpty = list.isEmpty()

  init {
    val filterOptions = { h: PortfolioStock -> h.holding.type == EquityType.OPTION }
    val sumCostNumber =
        if (isEmpty) 0.0 else list.asSequence().filterNot(filterOptions).map { it.costNumber }.sum()

    val todays = list.asSequence().filterNot(filterOptions).map { it.todayNumber }.toList()
    val isAnyDayInvalid = todays.any { it == null }

    val sumTotalAmountNumber =
        if (isAnyDayInvalid) 0.0
        else {
          val validTodays = todays.filterNotNull()
          if (validTodays.isEmpty()) 0.0 else validTodays.sum()
        }

    val isNoTotal = sumTotalAmountNumber.isZero()
    val sumTotalGainLossNumber = if (isNoTotal) 0.0 else sumTotalAmountNumber - sumCostNumber
    sumTotalAmount = sumTotalAmountNumber.asMoney()

    val sumTotalPercentNumber =
        if (sumCostNumber.isZero()) 0.0 else sumTotalGainLossNumber / sumCostNumber * 100
    sumTotalDirection = (sumTotalAmountNumber - sumCostNumber).asDirection()

    val todayChanges =
        list.asSequence().filterNot(filterOptions).map { it.todayChangeNumber }.toList()
    val isAnyChangeInvalid = todayChanges.any { it == null }

    val sumTodayChangeNumber =
        if (isAnyChangeInvalid) 0.0
        else {
          val validChanges = todayChanges.filterNotNull()
          if (validChanges.isEmpty()) 0.0 else validChanges.sum()
        }
    val sumTodayPercentNumber =
        if (isNoTotal) 0.0 else sumTodayChangeNumber / sumTotalAmountNumber * 100
    sumTodayDirection = sumTodayChangeNumber.asDirection()

    val totalSign = sumTotalDirection.sign
    gainLossDisplayString =
        "${totalSign}${sumTotalGainLossNumber.asMoney().display} (${totalSign}${sumTotalPercentNumber.asPercent().display})"

    val sumTodayChange = sumTodayChangeNumber.asMoney()
    val sign = sumTodayDirection.sign
    changeTodayDisplayString =
        "${sign}${sumTodayChange.display} (${sign}${sumTodayPercentNumber.asPercent().display})"
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
