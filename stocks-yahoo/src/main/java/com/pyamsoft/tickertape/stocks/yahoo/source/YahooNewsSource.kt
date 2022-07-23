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

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import com.pyamsoft.tickertape.stocks.yahoo.YahooApi
import com.pyamsoft.tickertape.stocks.yahoo.service.SearchService
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

internal class YahooNewsSource
@Inject
internal constructor(
    @YahooApi private val service: SearchService,
) : NewsSource {

  override suspend fun getNews(force: Boolean, symbol: StockSymbol): List<StockNews> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        try {
          val result = service.getNews(query = symbol.raw, count = 10)
          return@withContext result
              .news
              .asSequence()
              .map { news ->
                return@map StockNews.create(
                    id = news.uuid,
                    symbol = symbol,
                    publishedAt =
                        Instant.ofEpochMilli(news.providerPublishTime)
                            .atOffset(ZoneOffset.UTC)
                            .toLocalDateTime(),
                    title = news.title,
                    description = "",
                    link = news.link,
                    sourceName = news.publisher,
                )
              }
              .toList()
        } catch (e: Throwable) {
          if (e is HttpException) {
            // If YF delivers us a 404, then it just could not find any search results.
            // Empty list
            if (e.code() == 404) {
              return@withContext emptyList<StockNews>()
            }
          }

          // Otherwise rethrow the exception,
          throw e
        }
      }

  //  override suspend fun search(
  //      force: Boolean,
  //      query: String,
  //  ): List<SearchResult> =
  //      withContext(context = Dispatchers.IO) {
  //        Enforcer.assertOffMainThread()
  //
  //        if (query.isBlank()) {
  //          return@withContext emptyList()
  //        }
  //
  //        try {
  //          val result = service.performSearch(query, count = 20)
  //          return@withContext result
  //              .quotes
  //              .asSequence()
  //              // Remove duplicate listings
  //              .distinctBy { it.symbol }
  //              .map { quote ->
  //                return@map SearchResult.create(
  //                    symbol = quote.symbol.asSymbol(),
  //                    name = quote.name.orEmpty().asCompany(),
  //                    score = quote.score,
  //                    type = EquityType.from(quote.quoteType),
  //                )
  //              }
  //              .toList()
  //        } catch (e: Throwable) {
  //          if (e is HttpException) {
  //            // If YF delivers us a 404, then it just could not find any search results.
  //            // Empty list
  //            if (e.code() == 404) {
  //              return@withContext emptyList<SearchResult>()
  //            }
  //          }
  //
  //          // Otherwise rethrow the exception,
  //          throw e
  //        }
  //      }
}
