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

package com.pyamsoft.tickertape.stocks.cache.impl

import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.cache.StockCache
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class MemoryStockCacheImpl @Inject internal constructor() : StockCache {

  private val quotes by lazy { Quotes() }
  private val charts by lazy { Charts() }

  override suspend fun removeQuote(symbol: StockSymbol) =
      withContext(context = Dispatchers.Default) { quotes.remove(symbol) }

  override suspend fun removeAllQuotes() =
      withContext(context = Dispatchers.Default) { quotes.clear() }

  override suspend fun getQuotes(
      symbols: List<StockSymbol>,
      resolve: suspend (List<StockSymbol>) -> List<StockQuote>
  ): List<StockQuote> = withContext(context = Dispatchers.Default) { quotes.get(symbols, resolve) }

  override suspend fun removeChart(symbol: StockSymbol, range: StockChart.IntervalRange) =
      withContext(context = Dispatchers.Default) {
        val key =
            ChartKey(
                symbol = symbol,
                range = range,
            )
        return@withContext charts.remove(key)
      }

  override suspend fun removeAllCharts() =
      withContext(context = Dispatchers.Default) { charts.clear() }

  override suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
      resolve: suspend (List<StockSymbol>, StockChart.IntervalRange) -> List<StockChart>
  ): List<StockChart> =
      withContext(context = Dispatchers.Default) {
        val keys =
            symbols.map {
              ChartKey(
                  symbol = it,
                  range = range,
              )
            }
        return@withContext charts.get(keys) { resolve(it, range) }
      }

  private data class ChartKey(
      val symbol: StockSymbol,
      val range: StockChart.IntervalRange,
  )

  private class Quotes : BaseMemoryCacheImpl<StockSymbol, StockQuote>() {
    override fun getKeyFromValue(value: StockQuote): StockSymbol {
      return value.symbol
    }

    override fun getSymbolFromKey(key: StockSymbol): StockSymbol {
      return key
    }
  }

  private class Charts : BaseMemoryCacheImpl<ChartKey, StockChart>() {
    override fun getKeyFromValue(value: StockChart): ChartKey {
      return ChartKey(
          symbol = value.symbol,
          range = value.range,
      )
    }

    override fun getSymbolFromKey(key: ChartKey): StockSymbol {
      return key.symbol
    }
  }
}
