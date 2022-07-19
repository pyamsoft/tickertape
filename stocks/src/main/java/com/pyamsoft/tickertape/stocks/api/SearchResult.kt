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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.data.SearchResultImpl

interface SearchResult {

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val name: StockCompany

  @get:CheckResult val score: Long

  @get:CheckResult val type: EquityType

  companion object {
    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        name: StockCompany,
        score: Long,
        type: EquityType,
    ): SearchResult {
      return SearchResultImpl(
          symbol = symbol,
          name = name,
          score = score,
          type = type,
      )
    }
  }
}
