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

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol

@Stable
data class Ticker(
    val symbol: StockSymbol,
    val quote: StockQuote?,
    val chart: StockChart?,
) {

  companion object {

    @JvmField
    val COMPARATOR =
        Comparator<Ticker> { o1, o2 ->
          val q1 = o1.quote
          val q2 = o2.quote

          // If no quote, sort by symbol
          if (q1 == null && q2 == null) {
            return@Comparator o2.symbol.compareTo(o1.symbol)
          }

          // If either has a quote, it goes first
          if (q1 == null) {
            return@Comparator -1
          }

          // If either has a quote, it goes first
          if (q2 == null) {
            return@Comparator 1
          }

          val s1 = q1.currentSession
          val s2 = q2.currentSession
          return@Comparator s2.percent.compareTo(s1.percent)
        }
  }
}

@CheckResult
fun StockSymbol.isIndex(): Boolean {
  val raw = this.raw
  return raw.contains("=") || raw.startsWith("^")
}
