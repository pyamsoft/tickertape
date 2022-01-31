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
import com.pyamsoft.tickertape.stocks.data.StockOptionsQuoteImpl
import java.time.LocalDateTime

interface StockOptionsQuote : StockQuote {

  @CheckResult fun strike(): StockMoneyValue

  @CheckResult fun expireDate(): LocalDateTime

  companion object {
    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        company: StockCompany,
        strike: StockMoneyValue,
        equityType: EquityType,
        expireDate: LocalDateTime,
        regular: StockMarketSession,
        preMarket: StockMarketSession?,
        afterHours: StockMarketSession?,
        dataDelayBy: Long,
        dayPreviousClose: StockMoneyValue?,
        dayHigh: StockMoneyValue,
        dayLow: StockMoneyValue,
        dayOpen: StockMoneyValue,
        dayVolume: StockVolumeValue,
    ): StockOptionsQuote {
      return StockOptionsQuoteImpl(
          symbol = symbol,
          company = company,
          strike = strike,
          equityType = equityType,
          expireDate = expireDate,
          regular = regular,
          preMarket = preMarket,
          afterHours = afterHours,
          dataDelayBy = dataDelayBy,
          dayPreviousClose = dayPreviousClose,
          dayHigh = dayHigh,
          dayLow = dayLow,
          dayOpen = dayOpen,
          dayVolume = dayVolume,
      )
    }
  }
}
