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

interface StockQuote : BaseStockQuote {

  @get:CheckResult val extraDetails: Details

  interface Details {
    // Daily
    @get:CheckResult val averageDailyVolume3Month: StockVolumeValue?
    @get:CheckResult val averageDailyVolume10Day: StockVolumeValue?
    // 52 week
    @get:CheckResult val fiftyTwoWeekLowChange: StockMoneyValue?
    @get:CheckResult val fiftyTwoWeekLowChangePercent: StockPercent?
    @get:CheckResult val fiftyTwoWeekLow: StockMoneyValue?
    @get:CheckResult val fiftyTwoWeekHighChange: StockMoneyValue?
    @get:CheckResult val fiftyTwoWeekHighChangePercent: StockPercent?
    @get:CheckResult val fiftyTwoWeekHigh: StockMoneyValue?
    @get:CheckResult val fiftyTwoWeekRange: String

    // Moving average
    @get:CheckResult val fiftyDayAverage: StockMoneyValue?
    @get:CheckResult val fiftyDayAverageChange: StockMoneyValue?
    @get:CheckResult val fiftyDayAveragePercent: StockPercent?
    @get:CheckResult val twoHundredDayAverage: StockMoneyValue?
    @get:CheckResult val twoHundredDayAverageChange: StockMoneyValue?
    @get:CheckResult val twoHundredDayAveragePercent: StockPercent?

    // Market cap
    @get:CheckResult val marketCap: StockMarketCap?

    // Dividend and split
    @get:CheckResult val trailingAnnualDividendRate: Double?
    @get:CheckResult val trailingAnnualDividendYield: StockPercent?

    companion object {

      @JvmStatic
      @CheckResult
      fun empty(): Details {
        return object : Details {
          override val averageDailyVolume3Month: StockVolumeValue? = null
          override val averageDailyVolume10Day: StockVolumeValue? = null
          override val fiftyTwoWeekLowChange: StockMoneyValue? = null
          override val fiftyTwoWeekLowChangePercent: StockPercent? = null
          override val fiftyTwoWeekLow: StockMoneyValue? = null
          override val fiftyTwoWeekHighChange: StockMoneyValue? = null
          override val fiftyTwoWeekHighChangePercent: StockPercent? = null
          override val fiftyTwoWeekHigh: StockMoneyValue? = null
          override val fiftyTwoWeekRange: String = ""
          override val fiftyDayAverage: StockMoneyValue? = null
          override val fiftyDayAverageChange: StockMoneyValue? = null
          override val fiftyDayAveragePercent: StockPercent? = null
          override val twoHundredDayAverage: StockMoneyValue? = null
          override val twoHundredDayAverageChange: StockMoneyValue? = null
          override val twoHundredDayAveragePercent: StockPercent? = null
          override val marketCap: StockMarketCap? = null
          override val trailingAnnualDividendRate: Double? = null
          override val trailingAnnualDividendYield: StockPercent? = null
        }
      }

      @JvmStatic
      @CheckResult
      fun create(
          averageDailyVolume3Month: StockVolumeValue?,
          averageDailyVolume10Day: StockVolumeValue?,
          fiftyTwoWeekLowChange: StockMoneyValue?,
          fiftyTwoWeekLowChangePercent: StockPercent?,
          fiftyTwoWeekLow: StockMoneyValue?,
          fiftyTwoWeekHighChange: StockMoneyValue?,
          fiftyTwoWeekHighChangePercent: StockPercent?,
          fiftyTwoWeekHigh: StockMoneyValue?,
          fiftyTwoWeekRange: String,
          fiftyDayAverage: StockMoneyValue?,
          fiftyDayAverageChange: StockMoneyValue?,
          fiftyDayAveragePercent: StockPercent?,
          twoHundredDayAverage: StockMoneyValue?,
          twoHundredDayAverageChange: StockMoneyValue?,
          twoHundredDayAveragePercent: StockPercent?,
          marketCap: StockMarketCap?,
          trailingAnnualDividendRate: Double?,
          trailingAnnualDividendYield: StockPercent?,
      ): Details {
        return StockQuoteImpl.StockQuoteDetailsImpl(
            averageDailyVolume3Month,
            averageDailyVolume10Day,
            fiftyTwoWeekLowChange,
            fiftyTwoWeekLowChangePercent,
            fiftyTwoWeekLow,
            fiftyTwoWeekHighChange,
            fiftyTwoWeekHighChangePercent,
            fiftyTwoWeekHigh,
            fiftyTwoWeekRange,
            fiftyDayAverage,
            fiftyDayAverageChange,
            fiftyDayAveragePercent,
            twoHundredDayAverage,
            twoHundredDayAverageChange,
            twoHundredDayAveragePercent,
            marketCap,
            trailingAnnualDividendRate,
            trailingAnnualDividendYield,
        )
      }
    }
  }

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
        extraDetails: Details,
    ): StockQuote {
      return StockQuoteImpl(
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
          extraDetails,
      )
    }
  }
}
