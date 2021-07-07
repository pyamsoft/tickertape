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

package com.pyamsoft.tickertape.quote

import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol

data class QuotedStock internal constructor(val symbol: StockSymbol, val quote: StockQuote?) {

  companion object {

    @JvmField
    val COMPARATOR =
        Comparator<QuotedStock> { s1, s2 ->
          val q1 = s1.quote
          val q2 = s2.quote

          // If no quote, sort by symbol
          if (q1 == null && q2 == null) {
            return@Comparator s2.symbol.symbol().compareTo(s1.symbol.symbol(), ignoreCase = true)
          }

          // If either has a quote, it goes first
          if (q1 == null) {
            return@Comparator -1
          }

          // If either has a quote, it goes first
          if (q2 == null) {
            return@Comparator 1
          }

          // Sort by the change percent
          return@Comparator q2.regular().percent().value().compareTo(q1.regular().percent().value())
        }
  }
}

data class QuotedChart
internal constructor(val symbol: StockSymbol, val chart: StockChart?, val quote: StockQuote?)
