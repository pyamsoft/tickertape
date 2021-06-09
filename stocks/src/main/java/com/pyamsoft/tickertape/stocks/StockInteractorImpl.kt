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

package com.pyamsoft.tickertape.stocks

import com.pyamsoft.cachify.MemoryCacheStorage
import com.pyamsoft.cachify.cachify
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class StockInteractorImpl
@Inject
internal constructor(@InternalApi private val interactor: StockInteractor) : StockInteractor {

  private val quoteCache =
      cachify<List<StockQuote>, List<StockSymbol>>(
          storage = { listOf(MemoryCacheStorage.create(15, TimeUnit.MINUTES)) }) { symbols ->
        interactor.getQuotes(true, symbols)
      }

  override suspend fun getQuotes(force: Boolean, symbols: List<StockSymbol>): List<StockQuote> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          quoteCache.clear()
        }

        return@withContext quoteCache.call(symbols)
      }
}
