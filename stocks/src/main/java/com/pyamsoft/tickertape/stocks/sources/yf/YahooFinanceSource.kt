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
import com.pyamsoft.tickertape.stocks.data.StockMarketSessionImpl
import com.pyamsoft.tickertape.stocks.data.StockQuoteImpl
import com.pyamsoft.tickertape.stocks.service.QuoteService
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class YahooFinanceSource
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
        // If the symbol does not exist, these values will return null
        // We need all of these values to have a valid ticker
        .filterNot { it.shortName == null }
        .filterNot { it.regularMarketChange == null }
        .filterNot { it.regularMarketPrice == null }
        .filterNot { it.regularMarketChangePercent == null }
        // Only valid tickers here, so requireNotNull should never throw
        .map {
          StockQuoteImpl(
              symbol = it.symbol.asSymbol(),
              company = requireNotNull(it.shortName).asCompany(),
              regular =
                  StockMarketSessionImpl(
                      amount = requireNotNull(it.regularMarketChange).asMoney(),
                      direction = requireNotNull(it.regularMarketChange).asDirection(),
                      percent = requireNotNull(it.regularMarketChangePercent).asPercent(),
                      price = requireNotNull(it.regularMarketPrice).asMoney(),
                  ),
              afterHours =
                  if (it.postMarketChange != null &&
                      it.postMarketPrice != null &&
                      it.postMarketChangePercent != null) {
                    StockMarketSessionImpl(
                        amount = it.postMarketChange.asMoney(),
                        direction = it.postMarketChange.asDirection(),
                        percent = it.postMarketChangePercent.asPercent(),
                        price = it.postMarketPrice.asMoney(),
                    )
                  } else {
                    null
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
                "regularMarketPrice",
                "regularMarketChange",
                "regularMarketChangePercent",
                "postMarketPrice",
                "postMarketChange",
                "postMarketChangePercent",
            )
            .joinToString(",")
    private const val YF_QUOTE_FORMAT = "json"
    private const val YF_QUOTE_SOURCE = "https://query1.finance.yahoo.com/v7/finance/quote"
  }
}
