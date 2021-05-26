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

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.InternalApi
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.data.StockCompanyImpl
import com.pyamsoft.tickertape.stocks.data.StockDirectionImpl
import com.pyamsoft.tickertape.stocks.data.StockMarketSessionImpl
import com.pyamsoft.tickertape.stocks.data.StockMoneyValueImpl
import com.pyamsoft.tickertape.stocks.data.StockPercentImpl
import com.pyamsoft.tickertape.stocks.data.StockQuoteImpl
import com.pyamsoft.tickertape.stocks.data.StockSymbolImpl
import com.pyamsoft.tickertape.stocks.service.QuoteService
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooFinanceSource
@Inject
internal constructor(@InternalApi private val service: QuoteService) : QuoteSource {

  override suspend fun getQuotes(symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val result =
            service.getQuotes(
                url = YF_QUOTE_SOURCE,
                format = YF_QUOTE_FORMAT,
                fields = YF_QUOTE_FIELDS,
                symbols = symbols.joinToString(",") { it.symbol() })
        return@withContext result.quoteResponse.result.map {
          StockQuoteImpl(
              symbol = StockSymbolImpl(it.symbol),
              company = StockCompanyImpl(it.shortName),
              regular =
                  StockMarketSessionImpl(
                      amount = StockMoneyValueImpl(it.regularMarketChange),
                      direction = StockDirectionImpl(it.regularMarketChange),
                      percent = StockPercentImpl(it.regularMarketChangePercent),
                      price = StockMoneyValueImpl(it.regularMarketPrice),
                  ),
              afterHours =
                  if (it.postMarketChange != null &&
                      it.postMarketPrice != null &&
                      it.postMarketChangePercent != null) {
                    StockMarketSessionImpl(
                        amount = StockMoneyValueImpl(it.postMarketChange),
                        direction = StockDirectionImpl(it.postMarketChange),
                        percent = StockPercentImpl(it.postMarketChangePercent),
                        price = StockMoneyValueImpl(it.postMarketPrice),
                    )
                  } else {
                    null
                  })
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
