/*
 * Copyright 2023 pyamsoft
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
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.cache.OptionsCache
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class MemoryOptionsCacheImpl @Inject internal constructor() :
    BaseMemoryCacheImpl<MemoryOptionsCacheImpl.OptionsKey, StockOptions>(), OptionsCache {

  internal data class OptionsKey(
      val symbol: StockSymbol,
      val expirationDate: LocalDate?,
  )

  override suspend fun removeOption(
      symbol: StockSymbol,
      expirationDate: LocalDate?,
  ) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext remove(OptionsKey(symbol, expirationDate))
      }

  override suspend fun getOptions(
      symbols: List<StockSymbol>,
      expirationDate: LocalDate?,
      resolve: suspend (List<StockSymbol>, LocalDate?) -> List<StockOptions>
  ): List<StockOptions> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val keys =
            symbols.map { s ->
              OptionsKey(
                  symbol = s,
                  expirationDate = expirationDate,
              )
            }

        return@withContext get(keys) { resolve(it, expirationDate) }
      }

  override fun getKeyFromValue(value: StockOptions): OptionsKey {
    return OptionsKey(
        symbol = value.symbol,
        expirationDate = value.date,
    )
  }

  override fun getSymbolFromKey(key: OptionsKey): StockSymbol {
    return key.symbol
  }
}
