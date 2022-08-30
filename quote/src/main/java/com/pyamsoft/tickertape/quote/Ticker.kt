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
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol

data class Ticker(
    val symbol: StockSymbol,
    val quote: StockQuote?,
    val chart: StockChart?,
) {

  companion object {

    @JvmStatic
    @CheckResult
    fun createComparator(sort: QuoteSort): Comparator<Ticker> {
      return Comparator { o1, o2 ->
        val result = BASE_COMPARATOR.compare(o1, o2)
        if (result != null) {
          return@Comparator result
        } else {
          val q1 = o1.quote
          val q2 = o2.quote

          // If either has a quote, it goes first
          if (q1 == null) {
            return@Comparator -1
          }

          // If either has a quote, it goes first
          if (q2 == null) {
            return@Comparator 1
          }

          return@Comparator when (sort) {
            QuoteSort.PRE_MARKET -> sessionCompare(q1, q2) { preMarket }
            QuoteSort.AFTER_HOURS -> sessionCompare(q1, q2) { afterHours }
            else -> sessionCompare(q1, q2) { regular }
          }
        }
      }
    }

    @CheckResult
    private inline fun sessionCompare(
        q1: StockQuote,
        q2: StockQuote,
        sessionMapper: StockQuote.() -> StockMarketSession?
    ): Int {
      val s1 = sessionMapper(q1)
      val s2 = sessionMapper(q2)

      if (s1 == null && s2 == null) {
        return q1.regular.percent.compareTo(q2.regular.percent)
      }

      if (s1 == null) {
        return -1
      }

      if (s2 == null) {
        return 1
      }

      return s1.percent.compareTo(s2.percent)
    }

    private val BASE_COMPARATOR =
        NullableComparator<Ticker> { s1, s2 ->
          val q1 = s1.quote
          val q2 = s2.quote

          // If no quote, sort by symbol
          if (q1 == null && q2 == null) {
            return@NullableComparator s2.symbol.compareTo(s1.symbol)
          }

          // If either has a quote, it goes first
          if (q1 == null) {
            return@NullableComparator -1
          }

          // If either has a quote, it goes first
          if (q2 == null) {
            return@NullableComparator 1
          }

          // Fallthrough
          return@NullableComparator null
        }
  }
}

