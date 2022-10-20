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
    val currentPrice: DataPoint<Double>
    val targetHighPrice: DataPoint<Double>
    val targetLowPrice: DataPoint<Double>
    val targetMeanPrice: DataPoint<Double>
    val recommendationMean: DataPoint<Double>
    val numberOfAnalystOpinions: DataPoint<Long>
    val recommendationKey: Recommendation
    val profitMargin: DataPoint<Double>
    val operatingMargin: DataPoint<Double>
    val ebitdaMargin: DataPoint<Double>
    val grossMargin: DataPoint<Double>
    val returnOnAssets: DataPoint<Double>
    val returnOnEquity: DataPoint<Double>
    val totalRevenue: DataPoint<Long>
    val revenuePerShare: DataPoint<Double>
    val revenueGrowth: DataPoint<Double>
    val grossProfits: DataPoint<Long>
    val freeCashflow: DataPoint<Long>
    val operatingCashflow: DataPoint<Long>
    val ebitda: DataPoint<Long>
    val totalDebt: DataPoint<Long>
    val totalCashPerShare: DataPoint<Double>
    val quickRatio: DataPoint<Double>
    val currentRatio: DataPoint<Double>
    val debtToEquity: DataPoint<Double>
    val totalCash: DataPoint<Long>
    val earningsGrowth: DataPoint<Double>

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
    val beta: DataPoint<Double>
    val enterpriseValue: DataPoint<Long>
    val floatShares: DataPoint<Long>
    val sharesOutstanding: DataPoint<Long>
    val sharesShort: DataPoint<Long>
    val shortRatio: DataPoint<Double>
    val heldPercentInsiders: DataPoint<Double>
    val heldPercentInstitutions: DataPoint<Double>
    val shortPercentOfFloat: DataPoint<Double>
    val lastFiscalYearEnd: DataPoint<Long>
    val nextFiscalYearEnd: DataPoint<Long>
    val mostRecentQuarter: DataPoint<Long>
    val netIncomeToCommon: DataPoint<Long>
    val lastSplitDate: DataPoint<Long>
    val lastDividendValue: DataPoint<Double>
    val lastDividendDate: DataPoint<Long>
    val forwardEps: DataPoint<Double>
    val forwardPE: DataPoint<Double>
    val trailingEps: DataPoint<Double>
    val pegRatio: DataPoint<Double>
    val priceToBook: DataPoint<Double>
    val bookValue: DataPoint<Double>
    val enterpriseValueToRevenue: DataPoint<Double>
    val enterpriseValueToEbitda: DataPoint<Double>
    val fiftyTwoWeekChange: DataPoint<Double>
    val marketFiftyTwoWeekChange: DataPoint<Double>
  }

  interface Earnings {
    val earningsDate: DataPoint<Long>
    val earningsAverage: DataPoint<Double>
    val earningsLow: DataPoint<Double>
    val earningsHigh: DataPoint<Double>
    val revenueAverage: DataPoint<Long>
    val revenueLow: DataPoint<Long>
    val revenueHigh: DataPoint<Long>
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
