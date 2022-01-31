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

package com.pyamsoft.tickertape.stocks.cache.impl

import com.pyamsoft.cachify.CacheStorage
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
internal class MemoryStockCacheImpl @Inject internal constructor() : StockCache {

  // Use a mutex to avoid multiple quote lookups hitting the network instead of taking a trip to the
  // memory cache
  private val quoteMutex = Mutex()
  private val chartMutex = Mutex()

  private val quotes by lazy { mutableMapOf<StockSymbol, CacheStorage<StockQuote>>() }
  private val charts by lazy { mutableMapOf<ChartKey, CacheStorage<StockChart>>() }

  override suspend fun removeQuote(symbol: StockSymbol) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        quoteMutex.withLock { quotes[symbol]?.clear() }

        return@withContext
      }

  override suspend fun getQuotes(
      symbols: List<StockSymbol>,
      resolve: suspend (List<StockSymbol>) -> List<StockQuote>
  ): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext quoteMutex.withLock {
          val result = mutableListOf<StockQuote>()
          val stillNeeded = mutableListOf<StockSymbol>()
          for (symbol in symbols) {
            val quote = quotes[symbol]?.retrieve()
            if (quote != null) {
              result.add(quote)
            } else {
              stillNeeded.add(symbol)
            }
          }

          if (stillNeeded.isNotEmpty()) {
            val upstreamResult = resolve(stillNeeded)
            for (res in upstreamResult) {
              quotes.getOrPut(res.symbol()) { createNewMemoryCacheStorage() }
              quotes[res.symbol()]?.cache(res)
              result.add(res)
            }
          }

          return@withLock result
        }
      }

  override suspend fun removeChart(symbol: StockSymbol, range: StockChart.IntervalRange) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        chartMutex.withLock {
          val key = ChartKey(symbol, range)
          charts[key]?.clear()
        }

        return@withContext
      }

  override suspend fun getCharts(
      symbols: List<StockSymbol>,
      range: StockChart.IntervalRange,
      resolve: suspend (List<StockSymbol>, StockChart.IntervalRange) -> List<StockChart>
  ): List<StockChart> =
      withContext(context = Dispatchers.IO) {
        chartMutex.withLock {
          val result = mutableListOf<StockChart>()
          val stillNeeded = mutableListOf<StockSymbol>()
          for (symbol in symbols) {
            val key = ChartKey(symbol, range)
            val chart = charts[key]?.retrieve()
            if (chart != null) {
              result.add(chart)
            } else {
              stillNeeded.add(symbol)
            }
          }

          if (stillNeeded.isNotEmpty()) {
            val upstreamResult = resolve(stillNeeded, range)
            for (res in upstreamResult) {
              val key = ChartKey(res.symbol(), range)
              charts.getOrPut(key) { createNewMemoryCacheStorage() }
              charts[key]?.cache(res)
              result.add(res)
            }
          }

          return@withLock result
        }
      }

  private data class ChartKey(
      val symbol: StockSymbol,
      val range: StockChart.IntervalRange,
  )
}
