/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.stocks.remote.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// https://query2.finance.yahoo.com/v10/finance/quoteSummary/MSFT?modules=financialData,defaultKeyStatistics,calendarEvents,incomeStatementHistory,cashflowStatementHistory,balanceSheetHistory

@JsonClass(generateAdapter = true)
internal data class NetworkKeyStatisticsResponse internal constructor(val quoteSummary: Summary) {

  @JsonClass(generateAdapter = true)
  internal data class Summary internal constructor(val result: List<Statistics>) {

    @JsonClass(generateAdapter = true)
    internal data class Statistics
    internal constructor(
        val defaultKeyStatistics: Info?,
        val financialData: FinancialData?,
        val calendarEvents: Calendar?,
    ) {

      @JsonClass(generateAdapter = true)
      internal data class Calendar
      internal constructor(
          val exDividendDate: YFData?,
          val dividendDate: YFData?,
          val earnings: Earnings?,
      ) {

        @JsonClass(generateAdapter = true)
        internal data class Earnings
        internal constructor(
            val earningsDate: List<YFData>?,
            val earningsAverage: YFData?,
            val earningsLow: YFData?,
            val earningsHigh: YFData?,
            val revenueAverage: YFData?,
            val revenueLow: YFData?,
            val revenueHigh: YFData?,
        )
      }

      @JsonClass(generateAdapter = true)
      internal data class FinancialData
      internal constructor(
          val currentPrice: YFData?,
          val targetHighPrice: YFData?,
          val targetLowPrice: YFData?,
          val targetMeanPrice: YFData?,
          val recommendationMean: YFData?,
          val recommendationKey: String?,
          val numberOfAnalystOpinions: YFData?,
          val profitMargins: YFData?,
          val grossMargins: YFData?,
          val ebitdaMargins: YFData?,
          val operatingMargins: YFData?,
          val returnOnAssets: YFData?,
          val returnOnEquity: YFData?,
          val totalRevenue: YFData?,
          val revenuePerShare: YFData?,
          val revenueGrowth: YFData?,
          val grossProfits: YFData?,
          val freeCashflow: YFData?,
          val operatingCashflow: YFData?,
          val ebitda: YFData?,
          val totalDebt: YFData?,
          val totalCashPerShare: YFData?,
          val quickRatio: YFData?,
          val currentRatio: YFData?,
          val debtToEquity: YFData?,
          val totalCash: YFData?,
          val earningsGrowth: YFData?,
      )

      @JsonClass(generateAdapter = true)
      internal data class Info
      internal constructor(
          val enterpriseValue: YFData?,
          val floatShares: YFData?,
          val sharesOutstanding: YFData?,
          val sharesShort: YFData?,
          val shortRatio: YFData?,
          val heldPercentInsiders: YFData?,
          val heldPercentInstitutions: YFData?,
          val shortPercentOfFloat: YFData?,
          val beta: YFData?,
          val lastFiscalYearEnd: YFData?,
          val nextFiscalYearEnd: YFData?,
          val mostRecentQuarter: YFData?,
          val netIncomeToCommon: YFData?,
          val lastSplitDate: YFData?,
          val lastSplitFactor: String?,
          val lastDividendValue: YFData?,
          val lastDividendDate: YFData?,
          val forwardPE: YFData?,
          val forwardEps: YFData?,
          val pegRatio: YFData?,
          val trailingEps: YFData?,
          val priceToBook: YFData?,
          val bookValue: YFData?,
          val enterpriseToRevenue: YFData?,
          val enterpriseToEbitda: YFData?,
          @Json(name = "52WeekChange") val fiftyTwoWeekChange: YFData?,
          @Json(name = "SandP52WeekChange") val marketFiftyTwoWeekChange: YFData?,
      )
    }
  }

  @JsonClass(generateAdapter = true)
  internal data class YFData
  internal constructor(
      // Use Any because this may be a String, may be a Double, may be a Long, may be null
      //
      // Moshi encodes all number types as Double
      val raw: Any?,
      val fmt: String?,
      val longFmt: String?,
  )
}
