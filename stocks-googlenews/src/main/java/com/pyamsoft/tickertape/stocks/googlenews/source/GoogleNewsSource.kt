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

package com.pyamsoft.tickertape.stocks.googlenews.source

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.googlenews.GoogleNewsApi
import com.pyamsoft.tickertape.stocks.googlenews.service.NewsService
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class GoogleNewsSource
@Inject
internal constructor(@GoogleNewsApi private val service: NewsService) : NewsSource {

  override suspend fun getNews(
      force: Boolean,
      symbol: StockSymbol,
  ): List<StockNews> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val resp = service.getNews(query = "${symbol.raw} Stock")
        return@withContext resp.news.map { article ->
          return@map StockNews.create(
              id = article.id,
              symbol = symbol,
              title = article.title,
              description = article.description,
              link = article.link,
              publishedAt = article.publishDate,
              sourceName = article.newsSource,
          )
        }
      }
}
