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
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asVolume

@CheckResult
private fun newSession(state: MarketState): StockMarketSession {
  return object : StockMarketSession {
    override fun direction(): StockDirection {
      return 0.0.asDirection()
    }

    override fun price(): StockMoneyValue {
      return 1.0.asMoney()
    }

    override fun amount(): StockMoneyValue {
      return 1.0.asMoney()
    }

    override fun percent(): StockPercent {
      return 1.0.asPercent()
    }

    override fun state(): MarketState {
      return state
    }
  }
}

/** Should only be used in tests/preview */
@CheckResult
fun newTestQuote(symbol: StockSymbol): StockQuote {
  return object : StockQuote {
    override fun symbol(): StockSymbol {
      return symbol
    }

    override fun company(): StockCompany {
      return "TEST COMPANY".asCompany()
    }

    override fun type(): EquityType {
      return EquityType.STOCK
    }

    override fun realEquityType(): String {
      return "STOCK"
    }

    override fun regular(): StockMarketSession {
      return newSession(MarketState.REGULAR)
    }

    override fun preMarket(): StockMarketSession? {
      return null
    }

    override fun afterHours(): StockMarketSession? {
      return null
    }

    override fun dataDelayBy(): Long {
      return 0
    }

    override fun dayPreviousClose(): StockMoneyValue? {
      return 1.0.asMoney()
    }

    override fun dayOpen(): StockMoneyValue {
      return 2.0.asMoney()
    }

    override fun dayHigh(): StockMoneyValue {
      return 3.0.asMoney()
    }

    override fun dayLow(): StockMoneyValue {
      return 0.7.asMoney()
    }

    override fun dayVolume(): StockVolumeValue {
      return 100L.asVolume()
    }
  }
}
