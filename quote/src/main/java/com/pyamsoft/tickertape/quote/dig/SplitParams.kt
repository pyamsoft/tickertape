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

package com.pyamsoft.tickertape.quote.dig

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.squareup.moshi.JsonClass

@Stable
data class SplitParams(
    val symbol: StockSymbol,
    val holdingId: DbHolding.Id,
    val existingSplitId: DbSplit.Id,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        symbol = symbol.raw,
        holdingId = holdingId.raw,
        existingSplitId = existingSplitId.raw,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json
  internal constructor(
      val symbol: String,
      val holdingId: String,
      val existingSplitId: String,
  ) {

    @CheckResult
    fun fromJson(): SplitParams {
      return SplitParams(
          symbol = symbol.asSymbol(),
          holdingId = DbHolding.Id(holdingId),
          existingSplitId = DbSplit.Id(existingSplitId),
      )
    }
  }
}
