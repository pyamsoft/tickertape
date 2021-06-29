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
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.stocks.InternalApi
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asCompany
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.asVolume
import com.pyamsoft.tickertape.stocks.data.StockMarketSessionImpl
import com.pyamsoft.tickertape.stocks.data.StockQuoteImpl
import com.pyamsoft.tickertape.stocks.network.NetworkStock
import com.pyamsoft.tickertape.stocks.service.QuoteService
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class YahooQuoteSource
@Inject
internal constructor(@InternalApi private val service: QuoteService) : QuoteSource {

  @CheckResult
  private suspend fun fetchQuotes(symbols: List<StockSymbol>): List<StockQuote> {
    val result =
        service.getQuotes(
            url = YF_QUOTE_SOURCE,
            format = YF_QUOTE_FORMAT,
            fields = YF_QUOTE_FIELDS,
            symbols = symbols.joinToString(",") { it.symbol() })
    return result
        .quoteResponse
        .result
        .asSequence()
        .filterOnlyValidStockData()
        .map { stock ->
          StockQuoteImpl(
              symbol = stock.symbol.asSymbol(),
              company = requireNotNull(stock.shortName).asCompany(),
              dataDelayBy = requireNotNull(stock.exchangeDataDelayedBy),
              dayPreviousClose = stock.regularMarketPreviousClose?.asMoney(),
              dayHigh = requireNotNull(stock.regularMarketDayHigh).asMoney(),
              dayLow = requireNotNull(stock.regularMarketDayLow).asMoney(),
              dayOpen = requireNotNull(stock.regularMarketOpen).asMoney(),
              dayVolume = requireNotNull(stock.regularMarketVolume).asVolume(),
              regular =
                  StockMarketSessionImpl(
                      amount = requireNotNull(stock.regularMarketChange).asMoney(),
                      direction = requireNotNull(stock.regularMarketChange).asDirection(),
                      percent = requireNotNull(stock.regularMarketChangePercent).asPercent(),
                      price = requireNotNull(stock.regularMarketPrice).asMoney(),
                  ),
              afterHours =
                  if (!hasAfterHoursData(stock)) null
                  else {
                    StockMarketSessionImpl(
                        amount = requireNotNull(stock.postMarketChange).asMoney(),
                        direction = requireNotNull(stock.postMarketChange).asDirection(),
                        percent = requireNotNull(stock.postMarketChangePercent).asPercent(),
                        price = requireNotNull(stock.postMarketPrice).asMoney(),
                    )
                  })
        }
        .toList()
  }

  override suspend fun getQuotes(
      force: Boolean,
      symbols: List<StockSymbol>
  ): ResultWrapper<List<StockQuote>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(fetchQuotes(symbols))
        } catch (e: Throwable) {
          Timber.e(e, "Unable to fetch stock quotes from YF")
          ResultWrapper.failure(e)
        }
      }

  companion object {

    private val YF_QUOTE_FIELDS =
        listOf(
                "symbol",
                "shortName",
                "exchangeDataDelayedBy",
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
            )
            .joinToString(",")
    private const val YF_QUOTE_FORMAT = "json"
    private const val YF_QUOTE_SOURCE = "https://query1.finance.yahoo.com/v7/finance/quote"

    @JvmStatic
    @CheckResult
    private fun hasAfterHoursData(stock: NetworkStock): Boolean {
      return stock.run {
        postMarketChange != null && postMarketPrice != null && postMarketChangePercent != null
      }
    }

    @JvmStatic
    @CheckResult
    private fun Sequence<NetworkStock>.filterOnlyValidStockData(): Sequence<NetworkStock> {
      // If the symbol does not exist, these values will return null
      // We need all of these values to have a valid ticker
      return this.filterNot { it.shortName == null }
          .filterNot { it.regularMarketChange == null }
          .filterNot { it.regularMarketPrice == null }
          .filterNot { it.regularMarketChangePercent == null }
          .filterNot { it.regularMarketDayHigh == null }
          .filterNot { it.regularMarketDayLow == null }
          .filterNot { it.regularMarketDayRange == null }
          .filterNot { it.regularMarketVolume == null }
    }
  }
}
