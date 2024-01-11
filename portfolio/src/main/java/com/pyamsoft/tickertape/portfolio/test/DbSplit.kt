/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import java.time.Clock
import java.time.LocalDate

internal fun newTestSplit(clock: Clock): DbSplit {
  return object : DbSplit {
    override val id: DbSplit.Id = DbSplit.Id.EMPTY
    override val holdingId: DbHolding.Id = DbHolding.Id.EMPTY
    override val preSplitShareCount: StockShareValue = StockShareValue.NONE
    override val postSplitShareCount: StockShareValue = StockShareValue.NONE
    override val splitDate: LocalDate = LocalDate.now(clock)

    override fun preSplitShareCount(shareCount: StockShareValue): DbSplit {
      return this
    }

    override fun postSplitShareCount(shareCount: StockShareValue): DbSplit {
      return this
    }

    override fun splitDate(date: LocalDate): DbSplit {
      return this
    }
  }
}
