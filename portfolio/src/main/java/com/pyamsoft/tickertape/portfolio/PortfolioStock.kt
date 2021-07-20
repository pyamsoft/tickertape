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

import com.pyamsoft.tickertape.db.holding.DbHolding
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

  // Used in PortfolioStockList
  internal val costNumber = positions.sumOf { it.price().value() * it.shareCount().value() }
  internal val todayChangeNumber: Double?
  internal val todayNumber: Double?

  init {
    val totalSharesNumber =
        if (positions.isEmpty()) 0.0 else positions.sumOf { it.shareCount().value() }

    // Avoid -0.0 as a total change number
    if (totalSharesNumber.compareTo(0) == 0) {
      todayChangeNumber = 0.0
      todayNumber = 0.0
    } else {
      todayChangeNumber = quote?.quote?.regular()?.amount()?.value()?.times(totalSharesNumber)
      todayNumber = quote?.quote?.regular()?.price()?.value()?.times(totalSharesNumber)
    }

    val totalGainLossNumber = todayNumber?.minus(costNumber)

    val totalGainLossPercentNumber =
        totalGainLossNumber?.let {
          if (costNumber.compareTo(0) == 0) 0.0 else it / costNumber * 100
        }

    val todayChange = todayChangeNumber?.asMoney() ?: StockMoneyValue.none()
    totalDirection = totalGainLossNumber?.asDirection() ?: StockDirection.none()
    current = todayNumber?.asMoney() ?: StockMoneyValue.none()
    totalShares = totalSharesNumber.asShares()

    val totalGainLossPercent = totalGainLossPercentNumber?.asPercent() ?: StockPercent.none()
    val totalGainLoss = totalGainLossNumber?.asMoney() ?: StockMoneyValue.none()
    todayDirection = todayChangeNumber?.asDirection() ?: StockDirection.none()

    val sign = totalDirection.sign()
    gainLossDisplayString =
        "${sign}${totalGainLoss.asMoneyValue()} (${sign}${totalGainLossPercent.asPercentValue()})"

    changeTodayDisplayString = "${todayDirection.sign()}${todayChange.asMoneyValue()}"
  }
}
