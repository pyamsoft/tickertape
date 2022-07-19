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
    override val underlyingSymbol: StockSymbol,
    override val strike: StockMoneyValue?,
    override val expireDate: LocalDateTime,
    override val symbol: StockSymbol,
    override val company: StockCompany,
    override val type: EquityType,
    override val regular: StockMarketSession,
    override val preMarket: StockMarketSession?,
    override val afterHours: StockMarketSession?,
    override val dataDelayBy: Long,
    override val dayPreviousClose: StockMoneyValue?,
    override val dayHigh: StockMoneyValue,
    override val dayLow: StockMoneyValue,
    override val dayOpen: StockMoneyValue,
    override val dayVolume: StockVolumeValue,
) : StockOptionsQuote {

  override val currentSession: StockMarketSession = preMarket ?: afterHours ?: regular
}
