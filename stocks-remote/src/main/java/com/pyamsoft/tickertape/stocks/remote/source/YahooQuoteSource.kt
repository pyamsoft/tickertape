/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.stocks.remote.source

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asCompany
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMarketCap
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.asVolume
import com.pyamsoft.tickertape.stocks.parseUTCDate
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import com.pyamsoft.tickertape.stocks.remote.network.NetworkQuoteResponse
import com.pyamsoft.tickertape.stocks.remote.service.QuoteService
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class YahooQuoteSource
@Inject
internal constructor(@YahooApi private val service: QuoteService) : QuoteSource {

  override suspend fun getQuotes(symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.Default) {
        val result =
            service.getQuotes(
                fields = YF_QUOTE_FIELDS,
                symbols = symbols.joinToString(",") { it.raw },
            )

        val formatter = DATE_FORMATTER.get().requireNotNull()
        val localId = ZoneId.systemDefault()
        return@withContext result.quoteResponse.result
            .asSequence()
            .filterOnlyValidQuotes()
            // Remove duplicate listings
            .distinctBy { it.symbol }
            .map { stock ->
              if (stock.expireDate != null && stock.strike != null) {
                createOptionsQuote(stock, localId, formatter)
              } else {
                createQuote(stock)
              }
            }
            .toList()
      }

  companion object {

    @JvmStatic
    @CheckResult
    private fun createOptionsQuote(
        stock: NetworkQuoteResponse.Resp.Quote,
        localId: ZoneId,
        formatter: DateTimeFormatter,
    ): StockQuote {
      val underlyingSymbol = stock.underlyingSymbol.requireNotNull().asSymbol()
      val strikePrice = stock.strike?.asMoney()
      val expirationDate = parseUTCDate(stock.expireDate.requireNotNull(), localId)
      val companyName =
          "${underlyingSymbol.raw} ${expirationDate.format(formatter)}${if (strikePrice == null) "" else " ${strikePrice.display}"}"
      return StockOptionsQuote.create(
          underlyingSymbol = underlyingSymbol,
          strike = strikePrice,
          expireDate = expirationDate,
          symbol = stock.symbol.asSymbol(),
          equityType = EquityType.from(stock.quoteType.requireNotNull()),
          company = companyName.asCompany(),
          dataDelayBy = stock.exchangeDataDelayedBy.requireNotNull(),
          dayPreviousClose = stock.regularMarketPreviousClose?.asMoney(),
          dayHigh = stock.regularMarketDayHigh.requireNotNull().asMoney(),
          dayLow = stock.regularMarketDayLow.requireNotNull().asMoney(),
          dayOpen = stock.regularMarketOpen.requireNotNull().asMoney(),
          dayVolume = stock.regularMarketVolume.requireNotNull().asVolume(),
          regular =
              StockMarketSession.create(
                  amount = stock.regularMarketChange.requireNotNull().asMoney(),
                  direction = stock.regularMarketChange.requireNotNull().asDirection(),
                  percent = stock.regularMarketChangePercent.requireNotNull().asPercent(),
                  price = stock.regularMarketPrice.requireNotNull().asMoney(),
                  state = MarketState.REGULAR,
              ),
          afterHours =
              if (!hasAfterHoursData(stock)) null
              else {
                StockMarketSession.create(
                    amount = stock.postMarketChange.requireNotNull().asMoney(),
                    direction = stock.postMarketChange.requireNotNull().asDirection(),
                    percent = stock.postMarketChangePercent.requireNotNull().asPercent(),
                    price = stock.postMarketPrice.requireNotNull().asMoney(),
                    state = MarketState.POST,
                )
              },
          preMarket =
              if (!hasPreMarketData(stock)) null
              else {
                StockMarketSession.create(
                    amount = stock.preMarketChange.requireNotNull().asMoney(),
                    direction = stock.preMarketChange.requireNotNull().asDirection(),
                    percent = stock.preMarketChangePercent.requireNotNull().asPercent(),
                    price = stock.preMarketPrice.requireNotNull().asMoney(),
                    state = MarketState.PRE,
                )
              },
          extraDetails = createExtraDetails(stock),
      )
    }

    @JvmStatic
    @CheckResult
    private fun createExtraDetails(stock: NetworkQuoteResponse.Resp.Quote): StockQuote.Details {
      return StockQuote.Details.create(
          averageDailyVolume3Month = stock.averageDailyVolume3Month?.asVolume(),
          averageDailyVolume10Day = stock.averageDailyVolume10Day?.asVolume(),
          fiftyTwoWeekLowChange = stock.fiftyTwoWeekLowChange?.asMoney(),
          fiftyTwoWeekLowChangePercent = stock.fiftyTwoWeekLowChangePercent?.asPercent(),
          fiftyTwoWeekLow = stock.fiftyTwoWeekLow?.asMoney(),
          fiftyTwoWeekHighChange = stock.fiftyTwoWeekHighChange?.asMoney(),
          fiftyTwoWeekHighChangePercent = stock.fiftyTwoWeekHighChangePercent?.asPercent(),
          fiftyTwoWeekHigh = stock.fiftyTwoWeekHigh?.asMoney(),
          fiftyTwoWeekRange = stock.fiftyTwoWeekRange.orEmpty(),
          fiftyDayAverage = stock.fiftyDayAverage?.asMoney(),
          fiftyDayAverageChange = stock.fiftyDayAverageChange?.asMoney(),
          fiftyDayAveragePercent = stock.fiftyDayAveragePercent?.asPercent(),
          twoHundredDayAverage = stock.twoHundredDayAverage?.asMoney(),
          twoHundredDayAverageChange = stock.twoHundredDayAverageChange?.asMoney(),
          twoHundredDayAveragePercent = stock.twoHundredDayAveragePercent?.asPercent(),
          marketCap = stock.marketCap?.asMarketCap(),
          trailingAnnualDividendRate = stock.trailingAnnualDividendRate,
          trailingAnnualDividendYield =
              stock.trailingAnnualDividendYield?.asPercent(
                  // https://query1.finance.yahoo.com/v7/finance/quote?symbols=MSFT
                  //
                  // This comes as a float like 0.01003, but is meant to be 1%.
                  // Multiply it by 100 to get the "expected" percentage
                  isPercentageOutOfHundred = false,
              ),
      )
    }

    @JvmStatic
    @CheckResult
    private fun createQuote(stock: NetworkQuoteResponse.Resp.Quote): StockQuote {
      return StockQuote.create(
          symbol = stock.symbol.asSymbol(),
          equityType = EquityType.from(stock.quoteType.requireNotNull()),
          company = stock.name.orEmpty().asCompany(),
          dataDelayBy = stock.exchangeDataDelayedBy.requireNotNull(),
          dayPreviousClose = stock.regularMarketPreviousClose?.asMoney(),
          dayHigh = stock.regularMarketDayHigh?.asMoney(),
          dayLow = stock.regularMarketDayLow?.asMoney(),
          dayOpen = stock.regularMarketOpen?.asMoney(),
          dayVolume = stock.regularMarketVolume?.asVolume(),
          regular =
              StockMarketSession.create(
                  amount = stock.regularMarketChange.requireNotNull().asMoney(),
                  direction = stock.regularMarketChange.requireNotNull().asDirection(),
                  percent = stock.regularMarketChangePercent.requireNotNull().asPercent(),
                  price = stock.regularMarketPrice.requireNotNull().asMoney(),
                  state = MarketState.REGULAR,
              ),
          afterHours =
              if (!hasAfterHoursData(stock)) null
              else {
                StockMarketSession.create(
                    amount = stock.postMarketChange.requireNotNull().asMoney(),
                    direction = stock.postMarketChange.requireNotNull().asDirection(),
                    percent = stock.postMarketChangePercent.requireNotNull().asPercent(),
                    price = stock.postMarketPrice.requireNotNull().asMoney(),
                    state = MarketState.POST,
                )
              },
          preMarket =
              if (!hasPreMarketData(stock)) null
              else {
                StockMarketSession.create(
                    amount = stock.preMarketChange.requireNotNull().asMoney(),
                    direction = stock.preMarketChange.requireNotNull().asDirection(),
                    percent = stock.preMarketChangePercent.requireNotNull().asPercent(),
                    price = stock.preMarketPrice.requireNotNull().asMoney(),
                    state = MarketState.PRE,
                )
              },
          extraDetails = createExtraDetails(stock),
      )
    }

    private val YF_QUOTE_FIELDS =
        listOf(
                "symbol",
                "shortName",
                "longName",
                "exchangeDataDelayedBy",
                "marketState",
                // Options
                "strike",
                "expireDate",
                // Regular market
                "regularMarketPrice",
                "regularMarketChange",
                "regularMarketChangePercent",
                "regularMarketOpen",
                "regularMarketPreviousClose",
                "regularMarketDayHigh",
                "regularMarketDayLow",
                "regularMarketDayRange",
                "regularMarketVolume",
                // Post Market
                "postMarketPrice",
                "postMarketChange",
                "postMarketChangePercent",
                // Pre Market
                "preMarketPrice",
                "preMarketChange",
                "preMarketChangePercent",
                // Options
                "underlyingSymbol",

                // Extra details

                // Daily
                "averageDailyVolume3Month",
                "averageDailyVolume10Day",
                // 52 week
                "fiftyTwoWeekLowChange",
                "fiftyTwoWeekLowChangePercent",
                "fiftyTwoWeekLow",
                "fiftyTwoWeekHighChange",
                "fiftyTwoWeekHighChangePercent",
                "fiftyTwoWeekHigh",
                "fiftyTwoWeekRange",
                // Moving Average
                "fiftyDayAverage",
                "fiftyDayAverageChange",
                "fiftyDayAveragePercent",
                "twoHundredDayAverage",
                "twoHundredDayAverageChange",
                "twoHundredDayAveragePercent",
                // Market Cap
                "marketCap",
                // Dividends and Splits
                "trailingAnnualDividendRate",
                "trailingAnnualDividendYield",
            )
            .joinToString(",")
  }
}
