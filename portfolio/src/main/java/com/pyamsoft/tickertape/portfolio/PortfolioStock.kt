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

  @CheckResult
  fun todayDirection(): StockDirection {
    val today = todayNumber() ?: return StockDirection.none()
    val total = costNumber()
    return (today - total).asDirection()
  }

  @CheckResult
  internal fun todayChangeNumber(): Double? {
    val q = quote?.quote ?: return null
    return q.regular().amount().value() * totalSharesNumber()
  }

  @CheckResult
  fun todayChange(): StockMoneyValue? {
    return todayChangeNumber()?.asMoney()
  }

  @CheckResult
  internal fun todayNumber(): Double? {
    val q = quote?.quote ?: return null
    return q.regular().price().value() * totalSharesNumber()
  }

  @CheckResult
  private fun totalGainLossNumber(): Double? {
    val today = todayNumber() ?: return null
    return today - costNumber()
  }

  @CheckResult
  private fun totalGainLossPercentNumber(): Double? {
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
  fun averagePrice(): StockMoneyValue {
    val shares = totalSharesNumber()
    if (shares.compareTo(0) == 0) {
      return StockMoneyValue.none()
    }

    val value = costNumber() / shares
    return value.asMoney()
  }

  @CheckResult
  private fun totalSharesNumber(): Double {
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
private fun List<PortfolioStock>.sumTotalAmountNumber(): Double? {
  val todays = this.map { it.todayNumber() }
  return if (todays.any { it == null }) null
  else {
    todays.filterNotNull().sum()
  }
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalAmount(): StockMoneyValue? {
  return this.sumTotalAmountNumber()?.asMoney()
}

@CheckResult
private fun List<PortfolioStock>.sumTotalGainLossNumber(): Double? {
  val today = this.sumTotalAmountNumber() ?: return null
  return today - this.sumCostNumber()
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalGainLoss(): StockMoneyValue? {
  return this.sumTotalGainLossNumber()?.asMoney()
}

@CheckResult
private fun List<PortfolioStock>.sumTotalPercentNumber(): Double? {
  val gainLoss = this.sumTotalGainLossNumber() ?: return null
  val cost = this.sumCostNumber()
  return gainLoss / cost * 100
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalPercent(): StockPercent? {
  return this.sumTotalPercentNumber()?.asPercent()
}

@CheckResult
internal fun List<PortfolioStock>.sumTotalDirection(): StockDirection {
  val today = this.sumTotalAmountNumber() ?: return StockDirection.none()
  val total = this.sumCostNumber()
  return (today - total).asDirection()
}

@CheckResult
private fun List<PortfolioStock>.sumTodayChangeNumber(): Double? {
  val todays = this.map { it.todayChangeNumber() }
  return if (todays.any { it == null }) null
  else {
    todays.filterNotNull().sum()
  }
}

@CheckResult
internal fun List<PortfolioStock>.sumTodayChange(): StockMoneyValue? {
  return this.sumTodayChangeNumber()?.asMoney()
}

@CheckResult
private fun List<PortfolioStock>.sumTodayPercentNumber(): Double? {
  val change = this.sumTodayChangeNumber() ?: return null
  val total = this.sumTotalAmountNumber() ?: return null
  return change / total * 100
}

@CheckResult
internal fun List<PortfolioStock>.sumTodayPercent(): StockPercent? {
  return this.sumTodayPercentNumber()?.asPercent()
}

@CheckResult
internal fun List<PortfolioStock>.sumTodayDirection(): StockDirection {
  return this.sumTodayChangeNumber()?.asDirection() ?: return StockDirection.none()
}
