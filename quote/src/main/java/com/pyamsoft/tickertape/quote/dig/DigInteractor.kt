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
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockRecommendations
import com.pyamsoft.tickertape.stocks.api.StockSymbol

interface DigInteractor {

  @CheckResult
  suspend fun getStatistics(
      force: Boolean,
      symbol: StockSymbol,
  ): ResultWrapper<KeyStatistics>

  @CheckResult
  suspend fun getRecommendations(
      force: Boolean,
      symbol: StockSymbol,
  ): ResultWrapper<StockRecommendations>

  @CheckResult
  suspend fun getNews(
      force: Boolean,
      symbol: StockSymbol,
  ): ResultWrapper<List<StockNews>>

  @CheckResult
  suspend fun getChart(
      force: Boolean,
      symbol: StockSymbol,
      range: StockChart.IntervalRange,
  ): ResultWrapper<Ticker>

  @CheckResult
  suspend fun getCharts(
      force: Boolean,
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
  ): ResultWrapper<List<Ticker>>
}
