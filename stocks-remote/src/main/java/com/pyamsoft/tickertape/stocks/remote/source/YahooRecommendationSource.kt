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

import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import com.pyamsoft.tickertape.stocks.remote.service.RecommendationService
import com.pyamsoft.tickertape.stocks.remote.yahoo.YahooCrumbProvider
import com.pyamsoft.tickertape.stocks.sources.RecommendationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class YahooRecommendationSource
@Inject
internal constructor(
    @YahooApi private val service: RecommendationService,
    @YahooApi private val cookie: YahooCrumbProvider,
) : RecommendationSource {

  override suspend fun getRecommendations(symbol: StockSymbol): StockRecommendations =
      withContext(context = Dispatchers.Default) {
        try {
          val resp =
              cookie.withAuth { auth ->
                service.getRecommendations(
                    cookie = auth.cookie,
                    symbol = symbol.raw,
                    crumb = auth.crumb,
                )
              }

          val rec = resp.finance.result.first()
          return@withContext StockRecommendations.create(
              symbol = rec.symbol.asSymbol(),
              recommendations =
                  rec.recommendedSymbols
                      .asSequence()
                      .sortedByDescending { it.score }
                      .map { it.symbol }
                      .map { it.asSymbol() }
                      .toList(),
          )
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Unable to get recommendations for symbol: ${symbol.raw}")
            return@withContext StockRecommendations.create(
                symbol,
                emptyList(),
            )
          }
        }
      }
}
