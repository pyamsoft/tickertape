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

package com.pyamsoft.tickertape.watchlist

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.toSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class WatchlistInteractor @Inject internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val interactor: StockInteractor
) {

    @CheckResult
    private suspend fun getSymbols(force: Boolean): List<StockSymbol> =
        withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            return@withContext symbolQueryDao.query(force).map { it.symbol() }
        }

    @CheckResult
    suspend fun getQuotes(force: Boolean): List<WatchListViewState.QuotePair> =
        withContext(context = Dispatchers.IO) {
            var symbols = getSymbols(force)
            if (symbols.isEmpty()) {
                symbols = listOf(
                    "MSFT",
                    "AAPL",
                    "AMD",
                    "GME",
                    "BB",
                    "AMC",
                    "CLOV",
                    "VTI",
                    "VOO"
                ).map { it.toSymbol() }
            }

            // If we have no symbols, don't even make the trip
            if (symbols.isEmpty()) {
                return@withContext emptyList()
            }

            val quotePairs = mutableListOf<WatchListViewState.QuotePair>()
            val quotes = interactor.getQuotes(force, symbols)
            for (symbol in symbols) {
                val quote = quotes.find { it.symbol().symbol() == symbol.symbol() }
                quotePairs.add(
                    WatchListViewState.QuotePair(
                        symbol = symbol,
                        quote = quote,
                        error = if (quote == null) Throwable("Missing quote for $symbol") else null
                    )
                )
            }
            return@withContext quotePairs
        }

}
