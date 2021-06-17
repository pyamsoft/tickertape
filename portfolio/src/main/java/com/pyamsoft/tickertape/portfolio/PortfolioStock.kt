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
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShare

data class PortfolioStock
internal constructor(
    val holding: DbHolding,
    val positions: List<DbPosition>,
    val quote: QuotedStock?,
) {

  @CheckResult
  private fun totalPriceValue(): Double {
    return positions.sumOf { it.price().value() }
  }

  @CheckResult
  fun totalPrice(): StockMoneyValue {
    return totalPriceValue().asMoney()
  }

  @CheckResult
  fun averagePrice(): StockMoneyValue {
    val shares = totalSharesValue()
    if (shares.compareTo(0) == 0) {
      return NO_PRICE
    }

    val value = totalPriceValue() / shares
    return value.asMoney()
  }

  @CheckResult
  private fun totalSharesValue(): Double {
    return positions.sumOf { it.shareCount().value() }
  }

  @CheckResult
  fun totalShares(): StockShareValue {
    return totalSharesValue().asShare()
  }

  companion object {

    private val NO_PRICE = 0.0.asMoney()
  }
}
