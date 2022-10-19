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

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val quote: StockQuote?

  @get:CheckResult val earnings: Earnings?

  @get:CheckResult val financials: Financials?

  @get:CheckResult val info: Info?

  @CheckResult fun withQuote(quote: StockQuote?): KeyStatistics

  interface Financials {
    val currentPrice: DataPoint
    val targetHighPrice: DataPoint
    val targetLowPrice: DataPoint
    val targetMeanPrice: DataPoint
    val recommendationMean: DataPoint
    val numberOfAnalystOpinions: DataPoint
    val recommendationKey: Recommendation
    val profitMargin: DataPoint
    val operatingMargin: DataPoint
    val ebitdaMargin: DataPoint
    val grossMargin: DataPoint
    val returnOnAssets: DataPoint
    val returnOnEquity: DataPoint
    val totalRevenue: DataPoint
    val revenuePerShare: DataPoint
    val revenueGrowth: DataPoint
    val grossProfits: DataPoint
    val freeCashflow: DataPoint
    val operatingCashflow: DataPoint
    val ebitda: DataPoint
    val totalDebt: DataPoint
    val totalCashPerShare: DataPoint
    val quickRatio: DataPoint
    val currentRatio: DataPoint
    val debtToEquity: DataPoint
    val totalCash: DataPoint
    val earningsGrowth: DataPoint

    enum class Recommendation {
      BUY,
      SELL,
      HOLD,
      STRONG_BUY,
      UNDERPERFORM,
      UNKNOWN,
    }
  }

  interface Info {
    val beta: DataPoint
    val enterpriseValue: DataPoint
    val floatShares: DataPoint
    val sharesOutstanding: DataPoint
    val sharesShort: DataPoint
    val shortRatio: DataPoint
    val heldPercentInsiders: DataPoint
    val heldPercentInstitutions: DataPoint
    val shortPercentOfFloat: DataPoint
    val lastFiscalYearEnd: DataPoint
    val nextFiscalYearEnd: DataPoint
    val mostRecentQuarter: DataPoint
    val netIncomeToCommon: DataPoint
    val lastSplitDate: DataPoint
    val lastDividendValue: DataPoint
    val lastDividendDate: DataPoint
    val forwardEps: DataPoint
    val forwardPE: DataPoint
    val trailingEps: DataPoint
    val pegRatio: DataPoint
    val priceToBook: DataPoint
    val bookValue: DataPoint
    val enterpriseValueToRevenue: DataPoint
    val enterpriseValueToEbitda: DataPoint
    val fiftyTwoWeekChange: DataPoint
    val marketFiftyTwoWeekChange: DataPoint
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
    val fmt: String?

    companion object {

      val EMPTY =
          object : DataPoint {

            override val raw: Double = 0.0

            override val fmt: String? = null
          }
    }
  }

  companion object {
    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        quote: StockQuote?,
        earnings: Earnings?,
        financials: Financials?,
        info: Info?,
    ): KeyStatistics {
      return KeyStatisticsImpl(
          symbol = symbol,
          quote = quote,
          earnings = earnings,
          financials = financials,
          info = info,
      )
    }
  }
}
