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
import com.pyamsoft.tickertape.stocks.api.StockTops
import com.pyamsoft.tickertape.stocks.api.StockTrends
import com.pyamsoft.tickertape.stocks.api.asCompany
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.asVolume
import com.pyamsoft.tickertape.stocks.data.StockMarketSessionImpl
import com.pyamsoft.tickertape.stocks.data.StockQuoteImpl
import com.pyamsoft.tickertape.stocks.data.StockTopsImpl
import com.pyamsoft.tickertape.stocks.data.StockTrendsImpl
import com.pyamsoft.tickertape.stocks.service.TopService
import com.pyamsoft.tickertape.stocks.sources.TopSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooTopSource
@Inject
internal constructor(@InternalApi private val service: TopService) : TopSource {

  override suspend fun getTrending(force: Boolean, count: Int): StockTrends =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val result = service.getTrending(count)
        return@withContext result
            .finance
            .result
            .asSequence()
            .filterOnlyValidTrending()
            .map { trend ->
              StockTrendsImpl(
                  symbols =
                      trend
                          .quotes
                          .requireNotNull()
                          .asSequence()
                          .filterOnlyValidTrends()
                          .map { it.symbol.requireNotNull().asSymbol() }
                          .toList(),
              )
            }
            .first()
      }

  override suspend fun getDayGainers(force: Boolean, count: Int): StockTops {
    return getTops(TopType.GAINERS, count)
  }

  override suspend fun getDayLosers(force: Boolean, count: Int): StockTops {
    return getTops(TopType.LOSERS, count)
  }

  override suspend fun getMostShorted(force: Boolean, count: Int): StockTops {
    return getTops(TopType.SHORTED, count)
  }

  @CheckResult
  private suspend fun getTops(type: TopType, count: Int): StockTops =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val result =
            when (type) {
              TopType.GAINERS -> service.getDayGainers(count)
              TopType.LOSERS -> service.getDayLosers(count)
              TopType.SHORTED -> service.getMostShorted(count)
            }
        return@withContext result
            .finance
            .result
            .asSequence()
            .filterOnlyValidTops()
            .map { top ->
              StockTopsImpl(
                  title = top.title.requireNotNull(),
                  description = top.description.requireNotNull(),
                  quotes =
                      top.quotes
                          .requireNotNull()
                          .asSequence()
                          .filterOnlyValidQuotes()
                          .map { stock ->
                            StockQuoteImpl(
                                symbol = stock.symbol.asSymbol(),
                                equityType = stock.quoteType.requireNotNull(),
                                company =
                                    requireNotNull(stock.longName ?: stock.shortName).asCompany(),
                                dataDelayBy = requireNotNull(stock.exchangeDataDelayedBy),
                                dayPreviousClose = stock.regularMarketPreviousClose?.asMoney(),
                                dayHigh = requireNotNull(stock.regularMarketDayHigh).asMoney(),
                                dayLow = requireNotNull(stock.regularMarketDayLow).asMoney(),
                                dayOpen = requireNotNull(stock.regularMarketOpen).asMoney(),
                                dayVolume = requireNotNull(stock.regularMarketVolume).asVolume(),
                                regular =
                                    StockMarketSessionImpl(
                                        amount =
                                            requireNotNull(stock.regularMarketChange).asMoney(),
                                        direction =
                                            requireNotNull(stock.regularMarketChange).asDirection(),
                                        percent =
                                            requireNotNull(stock.regularMarketChangePercent)
                                                .asPercent(),
                                        price = requireNotNull(stock.regularMarketPrice).asMoney(),
                                    ),
                                afterHours =
                                    if (!hasAfterHoursData(stock)) null
                                    else {
                                      StockMarketSessionImpl(
                                          amount = requireNotNull(stock.postMarketChange).asMoney(),
                                          direction =
                                              requireNotNull(stock.postMarketChange).asDirection(),
                                          percent =
                                              requireNotNull(stock.postMarketChangePercent)
                                                  .asPercent(),
                                          price = requireNotNull(stock.postMarketPrice).asMoney(),
                                      )
                                    })
                          }
                          .toList())
            }
            .first()
      }

  private enum class TopType {
    GAINERS,
    LOSERS,
    SHORTED
  }
}
