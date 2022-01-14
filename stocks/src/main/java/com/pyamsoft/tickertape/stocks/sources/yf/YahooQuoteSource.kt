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

package com.pyamsoft.tickertape.stocks.sources.yf

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.InternalApi
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asCompany
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.asVolume
import com.pyamsoft.tickertape.stocks.data.StockMarketSessionImpl
import com.pyamsoft.tickertape.stocks.data.StockOptionsQuoteImpl
import com.pyamsoft.tickertape.stocks.data.StockQuoteImpl
import com.pyamsoft.tickertape.stocks.network.NetworkQuoteResponse
import com.pyamsoft.tickertape.stocks.service.QuoteService
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooQuoteSource
@Inject
internal constructor(@InternalApi private val service: QuoteService) : QuoteSource {

  override suspend fun getQuotes(force: Boolean, symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val result =
            service.getQuotes(
                url = YF_QUOTE_SOURCE,
                format = YF_QUOTE_FORMAT,
                fields = YF_QUOTE_FIELDS,
                symbols = symbols.joinToString(",") { it.symbol() })

        val localId = ZoneId.systemDefault()
        return@withContext result
            .quoteResponse
            .result
            .asSequence()
            .filterOnlyValidQuotes()
            // Remove duplicate listings
            .distinctBy { it.symbol }
            .map { stock ->
              if (stock.expireDate != null && stock.strike != null) {
                createOptionsQuote(stock, localId)
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
        localId: ZoneId
    ): StockQuote {
      return StockOptionsQuoteImpl(
          symbol = stock.symbol.asSymbol(),
          equityType = EquityType.from(stock.quoteType.requireNotNull()),
          company = (stock.longName ?: stock.shortName).requireNotNull().asCompany(),
          strike = stock.strike.requireNotNull().asMoney(),
          expireDate = parseMarketTime(stock.expireDate.requireNotNull(), localId),
          dataDelayBy = stock.exchangeDataDelayedBy.requireNotNull(),
          dayPreviousClose = stock.regularMarketPreviousClose?.asMoney(),
          dayHigh = stock.regularMarketDayHigh.requireNotNull().asMoney(),
          dayLow = stock.regularMarketDayLow.requireNotNull().asMoney(),
          dayOpen = stock.regularMarketOpen.requireNotNull().asMoney(),
          dayVolume = stock.regularMarketVolume.requireNotNull().asVolume(),
          regular =
              StockMarketSessionImpl(
                  amount = stock.regularMarketChange.requireNotNull().asMoney(),
                  direction = stock.regularMarketChange.requireNotNull().asDirection(),
                  percent = stock.regularMarketChangePercent.requireNotNull().asPercent(),
                  price = stock.regularMarketPrice.requireNotNull().asMoney(),
                  state = MarketState.REGULAR,
              ),
          afterHours =
              if (!hasAfterHoursData(stock)) null
              else {
                StockMarketSessionImpl(
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
                StockMarketSessionImpl(
                    amount = stock.preMarketChange.requireNotNull().asMoney(),
                    direction = stock.preMarketChange.requireNotNull().asDirection(),
                    percent = stock.preMarketChangePercent.requireNotNull().asPercent(),
                    price = stock.preMarketPrice.requireNotNull().asMoney(),
                    state = MarketState.PRE,
                )
              },
      )
    }

    @JvmStatic
    @CheckResult
    private fun createQuote(stock: NetworkQuoteResponse.Resp.Quote): StockQuote {
      return StockQuoteImpl(
          symbol = stock.symbol.asSymbol(),
          equityType = EquityType.from(stock.quoteType.requireNotNull()),
          company = (stock.longName ?: stock.shortName).requireNotNull().asCompany(),
          dataDelayBy = stock.exchangeDataDelayedBy.requireNotNull(),
          dayPreviousClose = stock.regularMarketPreviousClose?.asMoney(),
          dayHigh = stock.regularMarketDayHigh.requireNotNull().asMoney(),
          dayLow = stock.regularMarketDayLow.requireNotNull().asMoney(),
          dayOpen = stock.regularMarketOpen.requireNotNull().asMoney(),
          dayVolume = stock.regularMarketVolume.requireNotNull().asVolume(),
          regular =
              StockMarketSessionImpl(
                  amount = stock.regularMarketChange.requireNotNull().asMoney(),
                  direction = stock.regularMarketChange.requireNotNull().asDirection(),
                  percent = stock.regularMarketChangePercent.requireNotNull().asPercent(),
                  price = stock.regularMarketPrice.requireNotNull().asMoney(),
                  state = MarketState.REGULAR,
              ),
          afterHours =
              if (!hasAfterHoursData(stock)) null
              else {
                StockMarketSessionImpl(
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
                StockMarketSessionImpl(
                    amount = stock.preMarketChange.requireNotNull().asMoney(),
                    direction = stock.preMarketChange.requireNotNull().asDirection(),
                    percent = stock.preMarketChangePercent.requireNotNull().asPercent(),
                    price = stock.preMarketPrice.requireNotNull().asMoney(),
                    state = MarketState.PRE,
                )
              },
      )
    }

    private val YF_QUOTE_FIELDS =
        listOf(
                "symbol",
                "shortName",
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
            )
            .joinToString(",")
    private const val YF_QUOTE_FORMAT = "json"
    private const val YF_QUOTE_SOURCE = "https://query1.finance.yahoo.com/v7/finance/quote"
  }
}
