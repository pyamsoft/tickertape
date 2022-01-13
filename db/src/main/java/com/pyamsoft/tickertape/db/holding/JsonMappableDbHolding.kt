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

package com.pyamsoft.tickertape.db.holding

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonMappableDbHolding
internal constructor(
    internal val id: DbHolding.Id,
    internal val symbol: StockSymbol,
    internal val type: EquityType,
    internal val side: TradeSide,
) : DbHolding {

  override fun id(): DbHolding.Id {
    return id
  }

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun type(): EquityType {
    return type
  }

  override fun side(): TradeSide {
    return side
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        type: EquityType,
        side: TradeSide,
    ): DbHolding {
      return JsonMappableDbHolding(
          id = DbHolding.Id(IdGenerator.generate()),
          symbol = symbol,
          type = type,
          side = side,
      )
    }

    @JvmStatic
    @CheckResult
    fun from(item: DbHolding): JsonMappableDbHolding {
      return if (item is JsonMappableDbHolding) item
      else {
        JsonMappableDbHolding(
            item.id(),
            item.symbol(),
            item.type(),
            item.side(),
        )
      }
    }
  }
}
