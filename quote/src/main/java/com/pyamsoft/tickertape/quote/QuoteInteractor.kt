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

package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class QuoteInteractor @Inject internal constructor(private val interactor: StockInteractor) {

  @CheckResult
  suspend fun getQuotes(force: Boolean, symbols: List<StockSymbol>): List<QuotedStock> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // If we have no symbols, don't even make the trip
        if (symbols.isEmpty()) {
          return@withContext emptyList()
        }

        val quotePairs = mutableListOf<QuotedStock>()
        val quotes = interactor.getQuotes(force, symbols)
        for (symbol in symbols) {
          val quote = quotes.firstOrNull { it.symbol() == symbol }
          quotePairs.add(QuotedStock(symbol = symbol, quote = quote))
        }
        return@withContext quotePairs
      }
}
