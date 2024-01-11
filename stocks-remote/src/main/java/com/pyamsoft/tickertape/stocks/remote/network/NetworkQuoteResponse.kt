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

package com.pyamsoft.tickertape.stocks.remote.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class NetworkQuoteResponse internal constructor(val quoteResponse: Resp) {

  @JsonClass(generateAdapter = true)
  internal data class Resp internal constructor(val result: List<Quote>) {

    @JsonClass(generateAdapter = true)
    internal data class Quote
    internal constructor(
        val symbol: String,
        val longName: String?,
        val shortName: String?,
        val quoteType: String?,
        val marketState: String?,
        val exchangeDataDelayedBy: Long?,
        // Options
        val underlyingSymbol: String?,
        val strike: Double?,
        val expireDate: Long?,
        // Regular market
        val regularMarketPrice: Double?,
        val regularMarketChange: Double?,
        val regularMarketChangePercent: Double?,
        val regularMarketPreviousClose: Double?,
        val regularMarketOpen: Double?,
        val regularMarketDayHigh: Double?,
        val regularMarketDayLow: Double?,
        val regularMarketDayRange: String?,
        val regularMarketVolume: Long?,
        // Post market
        val postMarketPrice: Double?,
        val postMarketChange: Double?,
        val postMarketChangePercent: Double?,
        val postMarketPreviousClose: Double?,
        // Pre market
        val preMarketPrice: Double?,
        val preMarketChange: Double?,
        val preMarketChangePercent: Double?,
        val preMarketPreviousClose: Double?,
        // Extra Details
        // Daily
        val averageDailyVolume3Month: Long?,
        val averageDailyVolume10Day: Long?,
        // 52 week
        val fiftyTwoWeekLowChange: Double?,
        val fiftyTwoWeekLowChangePercent: Double?,
        val fiftyTwoWeekLow: Double?,
        val fiftyTwoWeekHighChange: Double?,
        val fiftyTwoWeekHighChangePercent: Double?,
        val fiftyTwoWeekHigh: Double?,
        val fiftyTwoWeekRange: String?,
        // Moving Average
        val fiftyDayAverage: Double?,
        val fiftyDayAverageChange: Double?,
        val fiftyDayAveragePercent: Double?,
        val twoHundredDayAverage: Double?,
        val twoHundredDayAverageChange: Double?,
        val twoHundredDayAveragePercent: Double?,
        // Market Cap
        val marketCap: Long?,
        // Dividends and Splits
        val trailingAnnualDividendRate: Double?,
        val trailingAnnualDividendYield: Double?,
    ) {

      val name = shortName ?: longName
    }
  }
}
