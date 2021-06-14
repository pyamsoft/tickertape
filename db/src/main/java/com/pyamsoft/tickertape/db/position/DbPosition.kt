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

package com.pyamsoft.tickertape.db.position

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue

interface DbPosition {

  @CheckResult fun id(): Id

  @CheckResult fun holdingId(): DbHolding.Id

  @CheckResult fun price(): StockMoneyValue

  @CheckResult fun shareCount(): Int

  @CheckResult fun fractionalShareCount(): Float

  data class Id(val id: String) {

    @CheckResult
    fun isEmpty(): Boolean {
      return id.isBlank()
    }

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }
}
