/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.quote.test

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue
import com.pyamsoft.tickertape.stocks.api.asCompany

@CheckResult
private fun newTestSession(): StockMarketSession {
  return object : StockMarketSession {
    override val direction: StockDirection = StockDirection.NONE
    override val price: StockMoneyValue = StockMoneyValue.NONE
    override val amount: StockMoneyValue = StockMoneyValue.NONE
    override val percent: StockPercent = StockPercent.NONE
    override val state: MarketState = MarketState.REGULAR
  }
}

/** Should only be used in tests/preview */
@CheckResult
fun newTestQuote(symbol: StockSymbol): StockQuote {
  return object : StockQuote {
    override val symbol: StockSymbol = symbol
    override val company: StockCompany = "".asCompany()
    override val type: EquityType = EquityType.STOCK
    override val regular: StockMarketSession = newTestSession()
    override val preMarket: StockMarketSession? = null
    override val afterHours: StockMarketSession? = null
    override val dataDelayBy: Long = 0
    override val dayPreviousClose: StockMoneyValue? = null
    override val dayOpen: StockMoneyValue? = null
    override val dayHigh: StockMoneyValue? = null
    override val dayLow: StockMoneyValue? = null
    override val dayVolume: StockVolumeValue? = null
    override val currentSession: StockMarketSession = regular
    override val extraDetails: StockQuote.Details = StockQuote.Details.empty()
  }
}
