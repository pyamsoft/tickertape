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

  @CheckResult fun symbol(): StockSymbol

  @CheckResult fun company(): StockCompany

  @CheckResult fun type(): EquityType

  @CheckResult fun regular(): StockMarketSession

  @CheckResult fun preMarket(): StockMarketSession?

  @CheckResult fun afterHours(): StockMarketSession?

  @CheckResult fun dataDelayBy(): Long

  @CheckResult fun dayPreviousClose(): StockMoneyValue?

  @CheckResult fun dayOpen(): StockMoneyValue?

  @CheckResult fun dayHigh(): StockMoneyValue?

  @CheckResult fun dayLow(): StockMoneyValue?

  @CheckResult fun dayVolume(): StockVolumeValue?

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
          symbol = symbol,
          company = company,
          equityType = equityType,
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

@CheckResult
fun StockQuote.currentSession(): StockMarketSession {
  return preMarket() ?: afterHours() ?: regular()
}
