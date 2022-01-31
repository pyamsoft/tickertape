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

package com.pyamsoft.tickertape.stocks.yahoo.source

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockTops
import com.pyamsoft.tickertape.stocks.api.StockTrends
import com.pyamsoft.tickertape.stocks.api.asCompany
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.asVolume
import com.pyamsoft.tickertape.stocks.sources.TopSource
import com.pyamsoft.tickertape.stocks.yahoo.YahooApi
import com.pyamsoft.tickertape.stocks.yahoo.service.TopService
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooTopSource
@Inject
internal constructor(@YahooApi private val service: TopService) : TopSource {

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
              StockTrends.create(
                  symbols =
                      trend
                          .quotes
                          .requireNotNull()
                          .asSequence()
                          .filterOnlyValidTrends()
                          .map { it.symbol.requireNotNull().asSymbol() }
                          // Remove duplicate listings
                          .distinct()
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
              StockTops.create(
                  title = top.title.requireNotNull(),
                  description = top.description.requireNotNull(),
                  quotes =
                      top.quotes
                          .requireNotNull()
                          .asSequence()
                          .filterOnlyValidQuotes()
                          // Remove duplicate listings
                          .distinctBy { it.symbol }
                          .map { stock ->
                            StockQuote.create(
                                symbol = stock.symbol.asSymbol(),
                                equityType = EquityType.from(stock.quoteType.requireNotNull()),
                                company =
                                    (stock.longName ?: stock.shortName)
                                        .requireNotNull()
                                        .asCompany(),
                                dataDelayBy = stock.exchangeDataDelayedBy.requireNotNull(),
                                dayPreviousClose = stock.regularMarketPreviousClose?.asMoney(),
                                dayHigh = stock.regularMarketDayHigh.requireNotNull().asMoney(),
                                dayLow = stock.regularMarketDayLow.requireNotNull().asMoney(),
                                dayOpen = stock.regularMarketOpen.requireNotNull().asMoney(),
                                dayVolume = stock.regularMarketVolume.requireNotNull().asVolume(),
                                regular =
                                    StockMarketSession.create(
                                        amount =
                                            stock.regularMarketChange.requireNotNull().asMoney(),
                                        direction =
                                            stock
                                                .regularMarketChange
                                                .requireNotNull()
                                                .asDirection(),
                                        percent =
                                            stock
                                                .regularMarketChangePercent
                                                .requireNotNull()
                                                .asPercent(),
                                        price = stock.regularMarketPrice.requireNotNull().asMoney(),
                                        state = MarketState.REGULAR,
                                    ),
                                afterHours =
                                    if (!hasAfterHoursData(stock)) null
                                    else {
                                      StockMarketSession.create(
                                          amount =
                                              stock.postMarketChange.requireNotNull().asMoney(),
                                          direction =
                                              stock.postMarketChange.requireNotNull().asDirection(),
                                          percent =
                                              stock
                                                  .postMarketChangePercent
                                                  .requireNotNull()
                                                  .asPercent(),
                                          price = stock.postMarketPrice.requireNotNull().asMoney(),
                                          state = MarketState.POST,
                                      )
                                    },
                                preMarket =
                                    if (!hasPreMarketData(stock)) null
                                    else {
                                      StockMarketSession.create(
                                          amount = stock.preMarketChange.requireNotNull().asMoney(),
                                          direction =
                                              stock.preMarketChange.requireNotNull().asDirection(),
                                          percent =
                                              stock
                                                  .preMarketChangePercent
                                                  .requireNotNull()
                                                  .asPercent(),
                                          price = stock.preMarketPrice.requireNotNull().asMoney(),
                                          state = MarketState.PRE,
                                      )
                                    },
                            )
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
