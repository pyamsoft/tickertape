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

package com.pyamsoft.tickertape.stocks.network

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
    ) {

      @JsonClass(generateAdapter = true)
      internal data class Info
      internal constructor(
          val forwardEps: YFData,
      )
    }
  }

  @JsonClass(generateAdapter = true)
  internal data class YFData
  internal constructor(
      val raw: Double,
      val fmt: String,
  )
}
