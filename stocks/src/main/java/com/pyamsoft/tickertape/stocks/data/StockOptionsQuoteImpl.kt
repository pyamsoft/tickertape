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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue
import java.time.LocalDateTime

internal data class StockOptionsQuoteImpl(
    private val symbol: StockSymbol,
    private val company: StockCompany,
    private val strike: StockMoneyValue,
    private val equityType: EquityType,
    private val realEquityType: String,
    private val expireDate: LocalDateTime,
    private val regular: StockMarketSession,
    private val preMarket: StockMarketSession?,
    private val afterHours: StockMarketSession?,
    private val dataDelayBy: Long,
    private val dayPreviousClose: StockMoneyValue?,
    private val dayHigh: StockMoneyValue,
    private val dayLow: StockMoneyValue,
    private val dayOpen: StockMoneyValue,
    private val dayVolume: StockVolumeValue,
) : StockOptionsQuote {

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun type(): EquityType {
    return equityType
  }

  override fun realEquityType(): String {
    return realEquityType
  }

  override fun dataDelayBy(): Long {
    return dataDelayBy
  }

  override fun strike(): StockMoneyValue {
    return strike
  }

  override fun expireDate(): LocalDateTime {
    return expireDate
  }

  override fun company(): StockCompany {
    return company
  }

  override fun regular(): StockMarketSession {
    return regular
  }

  override fun preMarket(): StockMarketSession? {
    return preMarket
  }

  override fun afterHours(): StockMarketSession? {
    return afterHours
  }

  override fun dayPreviousClose(): StockMoneyValue? {
    return dayPreviousClose
  }

  override fun dayVolume(): StockVolumeValue {
    return dayVolume
  }

  override fun dayOpen(): StockMoneyValue {
    return dayOpen
  }

  override fun dayLow(): StockMoneyValue {
    return dayLow
  }

  override fun dayHigh(): StockMoneyValue {
    return dayHigh
  }
}
