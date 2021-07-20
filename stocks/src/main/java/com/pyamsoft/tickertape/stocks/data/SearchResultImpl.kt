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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockSymbol

internal data class SearchResultImpl(
    private val symbol: StockSymbol,
    private val name: StockCompany,
    private val score: Long,
    private val type: EquityType,
) : SearchResult {

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun name(): StockCompany {
    return name
  }

  override fun score(): Long {
    return score
  }

  override fun type(): EquityType {
    return type
  }
}
