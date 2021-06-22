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
import androidx.annotation.ColorInt
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
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
import com.pyamsoft.tickertape.stocks.api.asShare

data class PortfolioStock
internal constructor(
    val holding: DbHolding,
    val positions: List<DbPosition>,
    val quote: QuotedStock?,
) {

  @ColorInt
  @CheckResult
  fun directionColor(): Int {
    val today = todayNumber() ?: return DEFAULT_STOCK_COLOR
    val total = costNumber()
    return (today - total).asDirection().color()
  }

  @CheckResult
  internal fun todayNumber(): Double? {
    val q = quote?.quote ?: return null
    return q.regular().price().value() * totalSharesNumber()
  }

  @CheckResult
  internal fun totalGainLossNumber(): Double? {
    val today = todayNumber() ?: return null
    return today - costNumber()
  }

  @CheckResult
  internal fun totalGainLossPercentNumber(): Double? {
    val gainLoss = totalGainLossNumber() ?: return null
    val totalCost = costNumber()
    return gainLoss / totalCost * 100
  }

  @CheckResult
  fun totalGainLossPercent(): StockPercent? {
    return totalGainLossPercentNumber()?.asPercent()
  }

  @CheckResult
  fun totalGainLoss(): StockMoneyValue? {
    return totalGainLossNumber()?.asMoney()
  }

  @CheckResult
  fun current(): StockMoneyValue? {
    return todayNumber()?.asMoney()
  }

  @CheckResult
  internal fun costNumber(): Double {
    return positions.sumOf { it.price().value() * it.shareCount().value() }
  }

  @CheckResult
  fun cost(): StockMoneyValue {
    return costNumber().asMoney()
  }

  @CheckResult
  fun averagePrice(): StockMoneyValue {
    val shares = totalSharesNumber()
    if (shares.compareTo(0) == 0) {
      return StockMoneyValue.none()
    }

    val value = costNumber() / shares
    return value.asMoney()
  }

  @CheckResult
  internal fun totalSharesNumber(): Double {
    return positions.sumOf { it.shareCount().value() }
  }

  @CheckResult
  fun totalShares(): StockShareValue {
    return totalSharesNumber().asShare()
  }
}

@CheckResult
private fun List<PortfolioStock>.sumCostNumber(): Double {
  return this.map { it.costNumber() }.sum()
}

@CheckResult
internal fun List<PortfolioStock>.sumCost(): StockMoneyValue {
  return this.sumCostNumber().asMoney()
}

@CheckResult
private fun List<PortfolioStock>.sumTodayNumber(): Double? {
  val todays = this.map { it.todayNumber() }
  return if (todays.any { it == null }) null
  else {
    todays.filterNotNull().sum()
  }
}

@CheckResult
internal fun List<PortfolioStock>.sumToday(): StockMoneyValue? {
  return this.sumTodayNumber()?.asMoney()
}

@CheckResult
private fun List<PortfolioStock>.sumGainLossNumber(): Double? {
  val today = this.sumTodayNumber() ?: return null
  return today - this.sumCostNumber()
}

@CheckResult
internal fun List<PortfolioStock>.sumGainLoss(): StockMoneyValue? {
  return this.sumGainLossNumber()?.asMoney()
}

@CheckResult
private fun List<PortfolioStock>.sumPercentNumber(): Double? {
  val gainLoss = this.sumGainLossNumber() ?: return null
  val cost = this.sumCostNumber()
  return gainLoss / cost * 100
}

@CheckResult
internal fun List<PortfolioStock>.sumPercent(): StockPercent? {
  return this.sumPercentNumber()?.asPercent()
}

@CheckResult
internal fun List<PortfolioStock>.sumDirection(): StockDirection {
  val today = this.sumTodayNumber() ?: return StockDirection.none()
  val total = this.sumCostNumber()
  return (today - total).asDirection()
}
