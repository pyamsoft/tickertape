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

package com.pyamsoft.tickertape.stocks.remote.source

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.remote.api.NasdaqApi
import com.pyamsoft.tickertape.stocks.remote.network.NetworkNewsResponse
import com.pyamsoft.tickertape.stocks.remote.service.NewsService
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class NasdaqNewsSource
@Inject
internal constructor(@NasdaqApi private val service: NewsService) : NewsSource {

  @CheckResult
  private fun NetworkNewsResponse.NewsArticle.toNews(symbol: StockSymbol): StockNews {
    return StockNews.create(
        id = this.id,
        symbol = symbol,
        title = this.title,
        description = this.description,
        link = this.link,
        publishedAt = this.publishDate,
        sourceName = this.newsSource,
        imageUrl = "",
        tickers = this.tickers.map { it.asSymbol() },
    )
  }

  override suspend fun getNews(
      force: Boolean,
      symbols: List<StockSymbol>,
  ): List<StockNews> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext symbols
            .map { symbol ->
              val resp =
                  service.getNews(
                      symbol = symbol.raw,
                      userAgent = pickRandomUserAgent(),
                  )
              return@map resp.news.map { it.toNews(symbol) }
            }
            .flatten()
      }

  companion object {

    private val ALL_USER_AGENTS =
        setOf(
            // Firefox 103
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:103.0) Gecko/20100101 Firefox/103.0",
            // Chrome 104
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36",
        )

    @JvmStatic
    @CheckResult
    private fun pickRandomUserAgent(): String {
      return ALL_USER_AGENTS.random()
    }
  }
}