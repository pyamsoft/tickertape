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

package com.pyamsoft.tickertape.db.symbol

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonMappableDbSymbol
internal constructor(
    override val id: DbSymbol.Id,
    override val symbol: StockSymbol,
) : DbSymbol {

  companion object {

    @JvmStatic
    @CheckResult
    fun create(symbol: StockSymbol): DbSymbol {
      return JsonMappableDbSymbol(id = DbSymbol.Id(IdGenerator.generate()), symbol = symbol)
    }

    @JvmStatic
    @CheckResult
    fun from(item: DbSymbol): JsonMappableDbSymbol {
      return if (item is JsonMappableDbSymbol) item
      else {
        JsonMappableDbSymbol(
            item.id,
            item.symbol,
        )
      }
    }
  }
}
