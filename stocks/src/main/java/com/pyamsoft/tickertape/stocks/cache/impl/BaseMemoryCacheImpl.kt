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

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.CacheStorage
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.cache.createNewMemoryCacheStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal abstract class BaseMemoryCacheImpl<K : Any, V : Any> protected constructor() {

  // Use a mutex to avoid multiple quote lookups hitting the network instead of taking a trip to the
  // memory cache
  private val mutex = Mutex()

  private val cache by lazy { mutableMapOf<K, CacheStorage<V>>() }

  @CheckResult protected abstract fun getKeyFromValue(value: V): K

  @CheckResult protected abstract fun getSymbolFromKey(key: K): StockSymbol

  suspend fun remove(key: K) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        mutex.withLock { cache[key]?.clear() }

        return@withContext
      }

  @CheckResult
  suspend inline fun get(
      keys: List<K>,
      crossinline resolve: suspend (List<StockSymbol>) -> List<V>,
  ): List<V> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext mutex.withLock {
          val result = mutableListOf<V>()
          val stillNeeded = mutableListOf<StockSymbol>()
          for (key in keys) {
            val quote = cache[key]?.retrieve()
            if (quote != null) {
              result.add(quote)
            } else {
              stillNeeded.add(getSymbolFromKey(key))
            }
          }

          if (stillNeeded.isNotEmpty()) {
            val upstreamResult = resolve(stillNeeded)
            for (res in upstreamResult) {
              val key = getKeyFromValue(res)
              cache.getOrPut(key) { createNewMemoryCacheStorage() }
              cache[key]?.cache(res)
              result.add(res)
            }
          }

          return@withLock result
        }
      }
}
