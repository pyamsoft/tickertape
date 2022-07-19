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
import com.pyamsoft.tickertape.stocks.data.StockQuoteImpl

interface StockQuote {

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val company: StockCompany

  @get:CheckResult val type: EquityType

  @get:CheckResult val regular: StockMarketSession

  @get:CheckResult val preMarket: StockMarketSession?

  @get:CheckResult val afterHours: StockMarketSession?

  @get:CheckResult val dataDelayBy: Long

  @get:CheckResult val dayPreviousClose: StockMoneyValue?

  @get:CheckResult val dayOpen: StockMoneyValue?

  @get:CheckResult val dayHigh: StockMoneyValue?

  @get:CheckResult val dayLow: StockMoneyValue?

  @get:CheckResult val dayVolume: StockVolumeValue?

  @get:CheckResult val currentSession: StockMarketSession

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        company: StockCompany,
        equityType: EquityType,
        regular: StockMarketSession,
        preMarket: StockMarketSession?,
        afterHours: StockMarketSession?,
        dataDelayBy: Long,
        dayPreviousClose: StockMoneyValue?,
        dayHigh: StockMoneyValue?,
        dayLow: StockMoneyValue?,
        dayOpen: StockMoneyValue?,
        dayVolume: StockVolumeValue?,
    ): StockQuote {
      return StockQuoteImpl(
          symbol,
          company,
          equityType,
          regular,
          preMarket,
          afterHours,
          dataDelayBy,
          dayPreviousClose,
          dayHigh,
          dayLow,
          dayOpen,
          dayVolume,
      )
    }
  }
}
