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

package com.pyamsoft.tickertape.stocks.yahoo.network

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.squareup.moshi.JsonClass

// https://query2.finance.yahoo.com/v10/finance/quoteSummary/MSFT?modules=financialData,defaultKeyStatistics,calendarEvents,incomeStatementHistory,cashflowStatementHistory,balanceSheetHistory

@JsonClass(generateAdapter = true)
internal data class NetworkKeyStatisticsResponse internal constructor(val quoteSummary: Summary) {

  @JsonClass(generateAdapter = true)
  internal data class Summary internal constructor(val result: List<Statistics>) {

    @JsonClass(generateAdapter = true)
    internal data class Statistics
    internal constructor(
        val defaultKeyStatistics: Info,
        val financialData: FinancialData,
        val calendarEvents: Calendar,
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
      )

      @JsonClass(generateAdapter = true)
      internal data class Info
      internal constructor(
          val enterpriseValue: YFData?,
          val profitMargin: YFData?,
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
          val lastDividendValue: YFData?,
          val lastDividendDate: YFData?,
          val forwardPE: YFData?,
          val forwardEps: YFData?,
          val pegRatio: YFData?,
          val trailingEps: YFData?,
          val priceToBook: YFData?,
          val enterpriseToRevenue: YFData?,
          val enterpriseToEbitda: YFData?
      )
    }
  }

  @JsonClass(generateAdapter = true)
  internal data class YFData
  internal constructor(
      // Use Any because this may be a String, may be a Double, may be null
      val raw: Any?,
      val fmt: String?,
      val longFmt: String?,
  )
}

private data class YFDataPoint(
    override val raw: Double,
    override val fmt: String,
) : KeyStatistics.DataPoint {
  override val isEmpty: Boolean = false
}

@CheckResult
internal fun NetworkKeyStatisticsResponse.YFData?.asDataPoint(
    long: Boolean = false
): KeyStatistics.DataPoint {
  return if (this == null || this.raw == null || this.fmt == null) {
    KeyStatistics.DataPoint.EMPTY
  } else {
    YFDataPoint(
        raw =
            if (this.raw is Double) this.raw
            else if (this.raw is String && this.raw == "Infinity") Double.POSITIVE_INFINITY
            else throw IllegalArgumentException("Invalid YFData.raw value: ${this.raw}"),
        fmt = if (long) this.longFmt ?: this.fmt else this.fmt,
    )
  }
}
