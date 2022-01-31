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
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.asCompany
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import com.pyamsoft.tickertape.stocks.yahoo.YahooApi
import com.pyamsoft.tickertape.stocks.yahoo.service.SearchService
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

internal class YahooSearchSource
@Inject
internal constructor(@YahooApi private val service: SearchService) : SearchSource {

  override suspend fun search(
      force: Boolean,
      query: String,
  ): List<SearchResult> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (query.isBlank()) {
          return@withContext emptyList()
        }

        try {
          val result = service.performSearch(query, count = 20)
          return@withContext result
              .quotes
              .asSequence()
              // Remove duplicate listings
              .distinctBy { it.symbol }
              .map { quote ->
                val company = (quote.longname ?: quote.shortname).orEmpty().asCompany()
                return@map SearchResult.create(
                    symbol = quote.symbol.asSymbol(),
                    name = company,
                    score = quote.score,
                    type = EquityType.from(quote.quoteType),
                )
              }
              .toList()
        } catch (e: Throwable) {
          if (e is HttpException) {
            // If YF delivers us a 404, then it just could not find any search results.
            // Empty list
            if (e.code() == 404) {
              return@withContext emptyList<SearchResult>()
            }
          }

          // Otherwise rethrow the exception,
          throw e
        }
      }
}
