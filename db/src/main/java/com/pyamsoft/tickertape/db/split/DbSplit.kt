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

package com.pyamsoft.tickertape.db.split

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.IdType
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import java.time.LocalDateTime

interface DbSplit {

  @get:CheckResult val id: Id

  @get:CheckResult val holdingId: DbHolding.Id

  @get:CheckResult val preSplitShareCount: StockShareValue

  @get:CheckResult val postSplitShareCount: StockShareValue

  @get:CheckResult val splitDate: LocalDateTime

  @CheckResult fun preSplitShareCount(shareCount: StockShareValue): DbSplit

  @CheckResult fun postSplitShareCount(shareCount: StockShareValue): DbSplit

  @CheckResult fun splitDate(date: LocalDateTime): DbSplit

  data class Id(override val raw: String) : IdType {

    override val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }
}
