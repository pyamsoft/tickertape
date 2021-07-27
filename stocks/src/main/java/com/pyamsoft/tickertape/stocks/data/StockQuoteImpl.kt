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

import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue

internal data class StockQuoteImpl(
    private val symbol: StockSymbol,
    private val company: StockCompany,
    private val equityType: String,
    private val regular: StockMarketSession,
    private val afterHours: StockMarketSession?,
    private val dataDelayBy: Long,
    private val dayPreviousClose: StockMoneyValue?,
    private val dayHigh: StockMoneyValue,
    private val dayLow: StockMoneyValue,
    private val dayOpen: StockMoneyValue,
    private val dayVolume: StockVolumeValue,
) : StockQuote {

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun type(): String {
    return equityType
  }

  override fun dataDelayBy(): Long {
    return dataDelayBy
  }

  override fun company(): StockCompany {
    return company
  }

  override fun regular(): StockMarketSession {
    return regular
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
