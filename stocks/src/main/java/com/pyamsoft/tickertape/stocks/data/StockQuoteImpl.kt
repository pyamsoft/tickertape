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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketCap
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue

internal data class StockQuoteImpl(
    override val symbol: StockSymbol,
    override val company: StockCompany,
    override val type: EquityType,
    override val regular: StockMarketSession,
    override val preMarket: StockMarketSession?,
    override val afterHours: StockMarketSession?,
    override val dataDelayBy: Long,
    override val dayPreviousClose: StockMoneyValue?,
    override val dayHigh: StockMoneyValue?,
    override val dayLow: StockMoneyValue?,
    override val dayOpen: StockMoneyValue?,
    override val dayVolume: StockVolumeValue?,
    override val extraDetails: StockQuote.Details,
) : StockQuote {

  override val currentSession: StockMarketSession = preMarket ?: afterHours ?: regular

  internal data class StockQuoteDetailsImpl(
      override val averageDailyVolume3Month: StockVolumeValue?,
      override val averageDailyVolume10Day: StockVolumeValue?,
      // 52 week
      override val fiftyTwoWeekLowChange: StockMoneyValue?,
      override val fiftyTwoWeekLowChangePercent: StockPercent?,
      override val fiftyTwoWeekLow: StockMoneyValue?,
      override val fiftyTwoWeekHighChange: StockMoneyValue?,
      override val fiftyTwoWeekHighChangePercent: StockPercent?,
      override val fiftyTwoWeekHigh: StockMoneyValue?,
      override val fiftyTwoWeekRange: String,
      // Moving Average
      override val fiftyDayAverage: StockMoneyValue?,
      override val fiftyDayAverageChange: StockMoneyValue?,
      override val fiftyDayAveragePercent: StockPercent?,
      override val twoHundredDayAverage: StockMoneyValue?,
      override val twoHundredDayAverageChange: StockMoneyValue?,
      override val twoHundredDayAveragePercent: StockPercent?,
      // Market Cap
      override val marketCap: StockMarketCap?,
      // Dividends and Splits
      override val trailingAnnualDividendRate: Double?,
      override val trailingAnnualDividendYield: StockPercent?,
  ) : StockQuote.Details
}
