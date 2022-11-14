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

package com.pyamsoft.tickertape.quote.dig

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.base.BaseTickerInteractor
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockNewsList
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockSymbol

interface DigInteractor : BaseTickerInteractor {

  @CheckResult suspend fun getStatistics(symbol: StockSymbol): ResultWrapper<KeyStatistics>

  @CheckResult
  suspend fun getRecommendations(symbol: StockSymbol): ResultWrapper<StockRecommendations>

  @CheckResult suspend fun getNews(symbol: StockSymbol): ResultWrapper<StockNewsList>

  @CheckResult
  suspend fun getChart(
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
  ): ResultWrapper<Ticker>

  @CheckResult
  suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): ResultWrapper<List<Ticker>>

  interface Cache : BaseTickerInteractor.Cache {

    suspend fun invalidateStatistics(symbol: StockSymbol)

    suspend fun invalidateRecommendations(symbol: StockSymbol)

    suspend fun invalidateNews(symbol: StockSymbol)

    suspend fun invalidateChart(symbol: StockSymbol, range: StockChart.IntervalRange)

    suspend fun invalidateCharts(symbols: List<StockSymbol>, range: StockChart.IntervalRange)
  }
}
