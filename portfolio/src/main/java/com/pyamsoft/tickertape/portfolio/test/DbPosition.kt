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

package com.pyamsoft.tickertape.portfolio.test

import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.Clock
import java.time.LocalDate

internal fun newTestPosition(clock: Clock): DbPosition {
  return object : DbPosition {
    override val id: DbPosition.Id = DbPosition.Id.EMPTY
    override val holdingId: DbHolding.Id = DbHolding.Id.EMPTY
    override val price: StockMoneyValue = 1.0.asMoney()
    override val shareCount: StockShareValue = 5.0.asShares()
    override val purchaseDate: LocalDate = LocalDate.now(clock)

    override fun price(price: StockMoneyValue): DbPosition {
      return this
    }

    override fun shareCount(shareCount: StockShareValue): DbPosition {
      return this
    }

    override fun purchaseDate(purchaseDate: LocalDate): DbPosition {
      return this
    }
  }
}
