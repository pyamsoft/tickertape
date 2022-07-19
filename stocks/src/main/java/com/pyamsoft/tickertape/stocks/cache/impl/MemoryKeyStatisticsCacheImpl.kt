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
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.cache.KeyStatisticsCache
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
internal class MemoryKeyStatisticsCacheImpl @Inject internal constructor() : KeyStatisticsCache {

  // Use a mutex to avoid multiple quote lookups hitting the network instead of taking a trip to the
  // memory cache
  private val mutex = Mutex()

  private val statistics by lazy { mutableMapOf<StockSymbol, CacheStorage<KeyStatistics>>() }

  override suspend fun removeStatistics(symbol: StockSymbol) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        mutex.withLock { statistics[symbol]?.clear() }

        return@withContext
      }

  override suspend fun getStatistics(
      symbols: List<StockSymbol>,
      resolve: suspend (List<StockSymbol>) -> List<KeyStatistics>
  ): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext mutex.withLock {
          val result = mutableListOf<KeyStatistics>()
          val stillNeeded = mutableListOf<StockSymbol>()
          for (symbol in symbols) {
            val quote = statistics[symbol]?.retrieve()
            if (quote != null) {
              result.add(quote)
            } else {
              stillNeeded.add(symbol)
            }
          }

          if (stillNeeded.isNotEmpty()) {
            val upstreamResult = resolve(stillNeeded)
            for (res in upstreamResult) {
              statistics.getOrPut(res.symbol) { createNewMemoryCacheStorage() }
              statistics[res.symbol]?.cache(res)
              result.add(res)
            }
          }

          return@withLock result
        }
      }
}
