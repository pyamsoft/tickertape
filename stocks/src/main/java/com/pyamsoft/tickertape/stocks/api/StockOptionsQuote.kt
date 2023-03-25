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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.data.StockOptionsQuoteImpl
import java.time.LocalDate

interface StockOptionsQuote : StockQuote {

  @get:CheckResult val underlyingSymbol: StockSymbol

  @get:CheckResult val strike: StockMoneyValue?

  @get:CheckResult val expireDate: LocalDate

  companion object {
    @JvmStatic
    @CheckResult
    fun create(
        underlyingSymbol: StockSymbol,
        strike: StockMoneyValue?,
        expireDate: LocalDate,
        symbol: StockSymbol,
        company: StockCompany,
        equityType: EquityType,
        regular: StockMarketSession,
        preMarket: StockMarketSession?,
        afterHours: StockMarketSession?,
        dataDelayBy: Long,
        dayPreviousClose: StockMoneyValue?,
        dayHigh: StockMoneyValue,
        dayLow: StockMoneyValue,
        dayOpen: StockMoneyValue,
        dayVolume: StockVolumeValue,
        extraDetails: StockQuote.Details,
    ): StockOptionsQuote {
      return StockOptionsQuoteImpl(
          underlyingSymbol,
          strike,
          expireDate,
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
          extraDetails = extraDetails,
      )
    }
  }
}
