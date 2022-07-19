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
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.cache.OptionsCache
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
internal class MemoryOptionsCacheImpl @Inject internal constructor() : OptionsCache {

  // Use a mutex to avoid multiple quote lookups hitting the network instead of taking a trip to the
  // memory cache
  private val optionsMutex = Mutex()

  private val options by lazy { mutableMapOf<StockSymbol, CacheStorage<StockOptions>>() }

  override suspend fun removeOption(symbol: StockSymbol) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        optionsMutex.withLock { options[symbol]?.clear() }

        return@withContext
      }

  override suspend fun getOptions(
      symbols: List<StockSymbol>,
      resolve: suspend (List<StockSymbol>) -> List<StockOptions>,
  ): List<StockOptions> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext optionsMutex.withLock {
          val result = mutableListOf<StockOptions>()
          val stillNeeded = mutableListOf<StockSymbol>()
          for (symbol in symbols) {
            val quote = options[symbol]?.retrieve()
            if (quote != null) {
              result.add(quote)
            } else {
              stillNeeded.add(symbol)
            }
          }

          if (stillNeeded.isNotEmpty()) {
            val upstreamResult = resolve(stillNeeded)
            for (res in upstreamResult) {
              options.getOrPut(res.symbol) { createNewMemoryCacheStorage() }
              options[res.symbol]?.cache(res)
              result.add(res)
            }
          }

          return@withLock result
        }
      }
}
