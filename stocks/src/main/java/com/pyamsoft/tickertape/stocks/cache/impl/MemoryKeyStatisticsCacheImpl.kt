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

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.cache.KeyStatisticsCache
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class MemoryKeyStatisticsCacheImpl @Inject internal constructor() :
    BaseMemoryCacheImpl<StockSymbol, KeyStatistics>(), KeyStatisticsCache {

  override suspend fun removeStatistics(symbol: StockSymbol) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext remove(symbol)
      }

  override suspend fun getStatistics(
      symbols: List<StockSymbol>,
      resolve: suspend (List<StockSymbol>) -> List<KeyStatistics>
  ): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext get(symbols, resolve)
      }

  override fun getKeyFromValue(value: KeyStatistics): StockSymbol {
    return value.symbol
  }

  override fun getSymbolFromKey(key: StockSymbol): StockSymbol {
    return key
  }
}
