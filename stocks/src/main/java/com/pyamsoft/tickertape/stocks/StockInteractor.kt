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

package com.pyamsoft.tickertape.stocks

import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockScreener
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.KeyStatisticSource
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import com.pyamsoft.tickertape.stocks.sources.RecommendationSource
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import com.pyamsoft.tickertape.stocks.sources.TopSource

interface StockInteractor :
    QuoteSource,
    ChartSource,
    TopSource,
    OptionsSource,
    SearchSource,
    KeyStatisticSource,
    NewsSource,
    RecommendationSource {

  interface Cache {

    suspend fun invalidateSearch(query: String)

    suspend fun invalidateAllQuotes()

    suspend fun invalidateQuotes(symbols: List<StockSymbol>)

    suspend fun invalidateAllCharts()

    suspend fun invalidateCharts(
        symbols: List<StockSymbol>,
        range: StockChart.IntervalRange,
    )

    suspend fun invalidateOptions(symbols: List<StockSymbol>)

    suspend fun invalidateNews(symbols: List<StockSymbol>)

    suspend fun invalidateRecommendations(symbol: StockSymbol)

    suspend fun invalidateScreener(screener: StockScreener)

    suspend fun invalidateStatistics(symbols: List<StockSymbol>)

    suspend fun invalidateTrending()
  }
}
