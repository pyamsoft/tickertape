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

import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent

data class PortfolioStockList(val list: List<PortfolioStock>) {

  val sumTotalAmount: StockMoneyValue
  val sumTotalGainLoss: StockMoneyValue
  val sumTotalDirection: StockDirection
  val sumTodayDirection: StockDirection
  val gainLossDisplayString: String
  val changeTodayDisplayString: String

  init {
    val sumCostNumber = if (list.isEmpty()) 0.0 else list.map { it.costNumber }.sum()
    val todays = list.map { it.todayNumber }

    val sumTotalAmountNumber =
        if (todays.any { it == null }) null
        else {
          val validTodays = todays.filterNotNull()
          if (validTodays.isEmpty()) 0.0 else validTodays.sum()
        }
    val sumTotalGainLossNumber = sumTotalAmountNumber?.minus(sumCostNumber)
    sumTotalAmount = sumTotalAmountNumber?.asMoney() ?: StockMoneyValue.none()
    sumTotalGainLoss = sumTotalGainLossNumber?.asMoney() ?: StockMoneyValue.none()

    val sumTotalPercentNumber =
        sumTotalGainLossNumber?.let {
          if (sumCostNumber.compareTo(0) == 0) 0.0 else it / sumCostNumber * 100
        }
    val sumTotalPercent = sumTotalPercentNumber?.asPercent() ?: StockPercent.none()
    sumTotalDirection =
        sumTotalAmountNumber?.minus(sumCostNumber)?.asDirection() ?: StockDirection.none()

    val todayChanges = list.map { it.todayChangeNumber }
    val sumTodayChangeNumber =
        if (todayChanges.any { it == null }) null
        else {
          val validChanges = todayChanges.filterNotNull()
          if (validChanges.isEmpty()) 0.0 else validChanges.sum()
        }
    val sumTodayChange = sumTodayChangeNumber?.asMoney() ?: StockMoneyValue.none()
    val sumTodayPercentNumber =
        sumTodayChangeNumber?.let { change ->
          val total = sumTotalAmountNumber ?: return@let null
          if (total.compareTo(0) == 0) 0.0 else change / total * 100
        }
    val sumTodayPercent = sumTodayPercentNumber?.asPercent() ?: StockPercent.none()
    sumTodayDirection = sumTodayChangeNumber?.asDirection() ?: StockDirection.none()

    val totalSign = sumTotalDirection.sign()
    gainLossDisplayString =
        "${totalSign}${sumTotalGainLoss.asMoneyValue()} (${totalSign}${sumTotalPercent.asPercentValue()})"

    val sign = sumTodayDirection.sign()
    changeTodayDisplayString =
        "${sign}${sumTodayChange.asMoneyValue()} (${sign}${sumTodayPercent.asPercentValue()})"
  }
}
