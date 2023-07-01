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
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockNewsList
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.parseUTCDateTime
import com.pyamsoft.tickertape.stocks.remote.api.RobinhoodApi
import com.pyamsoft.tickertape.stocks.remote.network.NetworkNewsResponse
import com.pyamsoft.tickertape.stocks.remote.robinhood.RobinhoodToken
import com.pyamsoft.tickertape.stocks.remote.service.NewsService
import com.pyamsoft.tickertape.stocks.remote.storage.CookieProvider
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RobinhoodNewsSource
@Inject
internal constructor(
    @RobinhoodApi private val service: NewsService,
    @RobinhoodApi private val cookie: CookieProvider<RobinhoodToken>,
) : NewsSource {

  @CheckResult
  private fun NetworkNewsResponse.News.toNews(symbol: StockSymbol): StockNews {
    return StockNews.create(
        id = this.uuid,
        symbol = symbol,
        title = this.title,
        description = this.previewText,
        link = this.url,
        publishedAt = parseUTCDateTime(this.publishedAt),
        sourceName = this.source,
        imageUrl = this.imageUrl,
    )
  }

  override suspend fun getNews(symbols: List<StockSymbol>): List<StockNewsList> =
      withContext(context = Dispatchers.Default) {
        val newsList = mutableMapOf<StockSymbol, MutableSet<StockNews>>()
        for (s in symbols) {
          val resp =
              cookie.withAuth { auth ->
                service.getNews(
                    symbol = s.raw,
                    token = auth.accessToken,
                    userAgent = RH_USER_AGENT,
                )
              }
          val news = resp.results.map { it.toNews(s) }
          for (n in news) {
            val list = newsList.getOrPut(n.symbol) { mutableSetOf() }
            list.add(n)
          }
        }

        return@withContext newsList.map { entry ->
          StockNewsList.create(
              symbol = entry.key,
              news = entry.value.toList(),
          )
        }
      }

  companion object {
    private const val RH_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"
  }
}
