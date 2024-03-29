/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.remote.network.NetworkChartResponse
import com.pyamsoft.tickertape.stocks.remote.network.NetworkQuoteResponse
import com.pyamsoft.tickertape.stocks.remote.network.NetworkTopResponse
import com.pyamsoft.tickertape.stocks.remote.network.NetworkTrendingResponse

@CheckResult
internal fun Sequence<NetworkChartResponse.Resp.SymbolChart>.filterOnlyValidCharts():
    Sequence<NetworkChartResponse.Resp.SymbolChart> {
  return this.filter { it.isValidStockData() }
}

@CheckResult
private fun NetworkChartResponse.Resp.SymbolChart.isValidStockData(): Boolean {
  // We need all of these values to have a valid ticker
  if (response.isEmpty()) {
    return false
  }

  val chart = response.first()
  if (chart.meta?.chartPreviousClose == null) {
    return false
  }

  // We need indicators and timestamps that are not empty
  val time = chart.timestamp ?: return false
  val ind = chart.indicators ?: return false

  if (time.isEmpty()) {
    return false
  }

  val quote = ind.quote ?: return false
  val actuallyQuote = quote.firstOrNull() ?: return false

  val close = actuallyQuote.close ?: return false

  if (close.isEmpty()) {
    return false
  }

  return true
}

@CheckResult
internal fun hasAfterHoursData(stock: NetworkQuoteResponse.Resp.Quote): Boolean {
  return stock.run {
    postMarketChange != null && postMarketPrice != null && postMarketChangePercent != null
  }
}

@CheckResult
internal fun hasPreMarketData(stock: NetworkQuoteResponse.Resp.Quote): Boolean {
  return stock.run {
    val state = MarketState.from(marketState) ?: return@run false

    state == MarketState.PRE &&
        preMarketChange != null &&
        preMarketPrice != null &&
        preMarketChangePercent != null
  }
}

@CheckResult
internal fun Sequence<NetworkTrendingResponse.Resp.Trending>.filterOnlyValidTrending():
    Sequence<NetworkTrendingResponse.Resp.Trending> {
  // We need all of these values to have a valid trender
  return this.filterNot { it.quotes == null }
}

@CheckResult
internal fun Sequence<NetworkTopResponse.Resp.Top>.filterOnlyValidTops():
    Sequence<NetworkTopResponse.Resp.Top> {
  // We need all of these values to have a valid top mover
  return this.filterNot { it.id == null }
      .filterNot { it.title == null }
      .filterNot { it.description == null }
      .filterNot { it.quotes == null }
}

@CheckResult
internal fun Sequence<NetworkTrendingResponse.Resp.Trending.Quote>.filterOnlyValidTrends():
    Sequence<NetworkTrendingResponse.Resp.Trending.Quote> {
  return this.filterNot { it.symbol == null }
}

@CheckResult
internal fun Sequence<NetworkQuoteResponse.Resp.Quote>.filterOnlyValidQuotes():
    Sequence<NetworkQuoteResponse.Resp.Quote> {
  // If the symbol does not exist, these values will return null
  // We need all of these values to have a valid ticker
  return this.filterNot { it.regularMarketChange == null }
      .filterNot { it.regularMarketPrice == null }
      .filterNot { it.regularMarketChangePercent == null }
}
