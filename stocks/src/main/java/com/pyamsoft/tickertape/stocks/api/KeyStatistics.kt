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
    @get:CheckResult val currentPrice: DataPoint<Double>
    @get:CheckResult val targetHighPrice: DataPoint<Double>
    @get:CheckResult val targetLowPrice: DataPoint<Double>
    @get:CheckResult val targetMeanPrice: DataPoint<Double>
    @get:CheckResult val recommendationMean: DataPoint<Double>
    @get:CheckResult val numberOfAnalystOpinions: DataPoint<Long>
    @get:CheckResult val recommendationKey: Recommendation
    @get:CheckResult val profitMargin: DataPoint<Double>
    @get:CheckResult val operatingMargin: DataPoint<Double>
    @get:CheckResult val ebitdaMargin: DataPoint<Double>
    @get:CheckResult val grossMargin: DataPoint<Double>
    @get:CheckResult val returnOnAssets: DataPoint<Double>
    @get:CheckResult val returnOnEquity: DataPoint<Double>
    @get:CheckResult val totalRevenue: DataPoint<Long>
    @get:CheckResult val revenuePerShare: DataPoint<Double>
    @get:CheckResult val revenueGrowth: DataPoint<Double>
    @get:CheckResult val grossProfits: DataPoint<Long>
    @get:CheckResult val freeCashflow: DataPoint<Long>
    @get:CheckResult val operatingCashflow: DataPoint<Long>
    @get:CheckResult val ebitda: DataPoint<Long>
    @get:CheckResult val totalDebt: DataPoint<Long>
    @get:CheckResult val totalCashPerShare: DataPoint<Double>
    @get:CheckResult val quickRatio: DataPoint<Double>
    @get:CheckResult val currentRatio: DataPoint<Double>
    @get:CheckResult val debtToEquity: DataPoint<Double>
    @get:CheckResult val totalCash: DataPoint<Long>
    @get:CheckResult val earningsGrowth: DataPoint<Double>

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
    @get:CheckResult val beta: DataPoint<Double>
    @get:CheckResult val enterpriseValue: DataPoint<Long>
    @get:CheckResult val floatShares: DataPoint<Long>
    @get:CheckResult val sharesOutstanding: DataPoint<Long>
    @get:CheckResult val sharesShort: DataPoint<Long>
    @get:CheckResult val shortRatio: DataPoint<Double>
    @get:CheckResult val heldPercentInsiders: DataPoint<Double>
    @get:CheckResult val heldPercentInstitutions: DataPoint<Double>
    @get:CheckResult val shortPercentOfFloat: DataPoint<Double>
    @get:CheckResult val lastFiscalYearEnd: DataPoint<Long>
    @get:CheckResult val nextFiscalYearEnd: DataPoint<Long>
    @get:CheckResult val mostRecentQuarter: DataPoint<Long>
    @get:CheckResult val netIncomeToCommon: DataPoint<Long>
    @get:CheckResult val lastSplitDate: DataPoint<Long>
    @get:CheckResult val lastSplitFactor: String
    @get:CheckResult val lastDividendValue: DataPoint<Double>
    @get:CheckResult val lastDividendDate: DataPoint<Long>
    @get:CheckResult val forwardEps: DataPoint<Double>
    @get:CheckResult val forwardPE: DataPoint<Double>
    @get:CheckResult val trailingEps: DataPoint<Double>
    @get:CheckResult val pegRatio: DataPoint<Double>
    @get:CheckResult val priceToBook: DataPoint<Double>
    @get:CheckResult val bookValue: DataPoint<Double>
    @get:CheckResult val enterpriseValueToRevenue: DataPoint<Double>
    @get:CheckResult val enterpriseValueToEbitda: DataPoint<Double>
    @get:CheckResult val fiftyTwoWeekChange: DataPoint<Double>
    @get:CheckResult val marketFiftyTwoWeekChange: DataPoint<Double>
  }

  interface Earnings {
    @get:CheckResult val earningsDate: DataPoint<Long>
    @get:CheckResult val earningsAverage: DataPoint<Double>
    @get:CheckResult val earningsLow: DataPoint<Double>
    @get:CheckResult val earningsHigh: DataPoint<Double>
    @get:CheckResult val revenueAverage: DataPoint<Long>
    @get:CheckResult val revenueLow: DataPoint<Long>
    @get:CheckResult val revenueHigh: DataPoint<Long>
    @get:CheckResult val exDividendDate: DataPoint<Long>
    @get:CheckResult val dividendDate: DataPoint<Long>
  }

  interface DataPoint<T : Number> {
    val raw: T
    val fmt: String?
    val isEmpty: Boolean
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
