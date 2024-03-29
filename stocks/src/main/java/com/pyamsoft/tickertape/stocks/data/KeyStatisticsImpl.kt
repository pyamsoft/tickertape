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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol

internal data class KeyStatisticsImpl(
    override val symbol: StockSymbol,
    override val quote: StockQuote?,
    override val earnings: KeyStatistics.Earnings?,
    override val financials: KeyStatistics.Financials?,
    override val info: KeyStatistics.Info?,
) : KeyStatistics {

  override fun withQuote(quote: StockQuote?): KeyStatistics {
    return this.copy(quote = quote)
  }
}
