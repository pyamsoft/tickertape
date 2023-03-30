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

package com.pyamsoft.tickertape.quote.dig

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.squareup.moshi.JsonClass

@Stable
data class PortfolioDigParams(
    val symbol: StockSymbol,
    val equityType: EquityType,
    val lookupSymbol: StockSymbol?,
    val currentPrice: StockMoneyValue? = null,
    val holding: DbHolding? = null,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        symbol = symbol.raw,
        equityType = equityType.name,
        lookupSymbol = lookupSymbol?.raw,
        currentPrice = currentPrice?.value,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json
  internal constructor(
      val symbol: String,
      val equityType: String,
      val lookupSymbol: String?,
      val currentPrice: Double?,
  ) {

    @CheckResult
    fun fromJson(): PortfolioDigParams {
      return PortfolioDigParams(
          symbol = symbol.asSymbol(),
          equityType = EquityType.valueOf(equityType),
          lookupSymbol = lookupSymbol?.asSymbol(),
          currentPrice = currentPrice?.asMoney(),
      )
    }
  }
}
