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
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.asCompany
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.data.SearchResultImpl
import com.pyamsoft.tickertape.stocks.service.SearchService
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class YahooSearchSource
@Inject
internal constructor(@InternalApi private val service: SearchService) : SearchSource {

  override suspend fun search(
      force: Boolean,
      query: String,
  ): List<SearchResult> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (query.isBlank()) {
          return@withContext emptyList()
        }

        val result = service.performSearch(query)
        return@withContext result
            .quotes
            .asSequence()
            .filterOnlyValidResults()
            .map { quote ->
              val company = requireNotNull(quote.longname ?: quote.shortname).asCompany()
              return@map SearchResultImpl(
                  symbol = quote.symbol.asSymbol(),
                  name = company,
                  score = quote.score,
                  type =
                      when (quote.quoteType) {
                        "EQUITY", "ETF" -> SearchResult.Type.STOCK
                        "OPTION" -> SearchResult.Type.OPTION
                        else ->
                            throw IllegalArgumentException(
                                "Invalid SearchResult.Type: ${quote.quoteType}")
                      })
            }
            .toList()
      }
}