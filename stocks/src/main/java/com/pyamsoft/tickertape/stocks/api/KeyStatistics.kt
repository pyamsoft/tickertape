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
import com.pyamsoft.tickertape.stocks.data.KeyStatisticsImpl

interface KeyStatistics {

  @CheckResult fun symbol(): StockSymbol

  @CheckResult fun earnings(): Earnings

  @CheckResult fun financials(): Financials

  @CheckResult fun info(): Info

  interface Financials {
    val targetHighPrice: DataPoint
    val targetLowPrice: DataPoint
    val targetMeanPrice: DataPoint
    val recommendationMean: DataPoint
    val numberOfAnalystOpinions: DataPoint
    val recommendationKey: Recommendation

    enum class Recommendation {
      BUY,
      SELL,
      HOLD,
      UNKNOWN
    }
  }

  interface Info {
    val beta: DataPoint
    val enterpriseValue: DataPoint
    val profitMargin: DataPoint
    val floatShares: DataPoint
    val sharesOutstanding: DataPoint
    val sharesShort: DataPoint
    val shortRatio: DataPoint
    val heldPercentInsiders: DataPoint
    val heldPercentInstitutions: DataPoint
    val shortPercentOfFloat: DataPoint
    val impliedSharesOutstanding: DataPoint
    val lastFiscalYearEnd: DataPoint
    val nextFiscalYearEnd: DataPoint
    val mostRecentQuarter: DataPoint
    val earningsQuarterlyGrowth: DataPoint
    val netIncomeToCommon: DataPoint
    val lastSplitDate: DataPoint
    val lastDividendValue: DataPoint
    val lastDividendDate: DataPoint
    val forwardEps: DataPoint
    val trailingEps: DataPoint
  }

  interface Earnings {
    val earningsDate: DataPoint
    val earningsAverage: DataPoint
    val earningsLow: DataPoint
    val earningsHigh: DataPoint
    val revenueAverage: DataPoint
    val revenueLow: DataPoint
    val revenueHigh: DataPoint
  }

  interface DataPoint {
    val raw: Double
    val fmt: String
    val isEmpty: Boolean

    companion object {

      val EMPTY =
          object : DataPoint {

            override val raw: Double = 0.0

            override val fmt: String = ""

            override val isEmpty: Boolean = true
          }
    }
  }

  companion object {
    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        earnings: Earnings,
        financials: Financials,
        info: Info,
    ): KeyStatistics {
      return KeyStatisticsImpl(
          symbol = symbol,
          earnings = earnings,
          financials = financials,
          info = info,
      )
    }
  }
}
